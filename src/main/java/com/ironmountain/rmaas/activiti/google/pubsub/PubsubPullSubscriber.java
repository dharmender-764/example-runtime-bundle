package com.ironmountain.rmaas.activiti.google.pubsub;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.activiti.cloud.services.core.pageable.SecurityAwareProcessInstanceService;
import org.activiti.cloud.services.core.pageable.SecurityAwareTaskService;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.util.json.JSONException;
import org.activiti.engine.impl.util.json.JSONObject;
import org.activiti.runtime.api.model.ProcessInstance;
import org.activiti.runtime.api.model.ProcessInstance.ProcessInstanceStatus;
import org.activiti.runtime.api.model.Task;
import org.activiti.runtime.api.model.payloads.CompleteTaskPayload;
import org.activiti.runtime.api.model.payloads.StartProcessPayload;
import org.activiti.runtime.api.model.payloads.SuspendProcessPayload;
import org.activiti.runtime.api.query.Page;
import org.activiti.runtime.api.query.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import com.ironmountain.rmaas.activiti.google.spanner.SpannerApiClient;

@Component
public class PubsubPullSubscriber {

	private static final Logger logger = LoggerFactory.getLogger(PubsubPullSubscriber.class);

	@Value("${gcp.projectid}")
	private String projectId;

	@Value("${gcp.pubsub.pull.subscriptionid}")
	private String subscriptionId;

	@Value("${mortgage.process.definition.id}")
	private String mortgageProcessDefinitionId;

	@Autowired
	private SecurityAwareProcessInstanceService processInstanceService;

	@Autowired
	private SecurityAwareTaskService taskService;
	
	@Autowired
	private SpannerApiClient spannerApiClient;

	private Subscriber subscriber = null;

	@PostConstruct
	public void initiateMessageReceiver() {

		logger.info("******** Starting Google pub/sub pull subscriber with project id: " + projectId + " and subid: " + subscriptionId);

		ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId);
		// Instantiate an asynchronous message receiver
		MessageReceiver receiver = new MessageReceiver() {
			@Override
			public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
				String pubsubMessage = message.getData().toStringUtf8();
				logger.info("Got pub/sub message with Id: " + message.getMessageId() + " and data: " + pubsubMessage);
				consumer.ack();
				
				processPubsubMessage(pubsubMessage);
			}

		};

		try {
			// Create a subscriber for "my-subscription-id" bound to the message receiver
			ServiceAccountCredentials credentials = ServiceAccountCredentials.fromStream(new FileInputStream("/root/rmaas-dit-1-2cb23d40da29.json"));
			subscriber = Subscriber.newBuilder(subscriptionName, receiver).setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();

			subscriber.addListener(new Subscriber.Listener() {
				public void failed(Subscriber.State from, Throwable failure) {
					logger.error("error getting pubsub message", failure);
					failure.printStackTrace();
				}
			}, MoreExecutors.directExecutor());

			subscriber.startAsync();
		} catch (Exception e) {
			logger.error("Could not start google pub/sub pull subscriber for project id: " + projectId + " and subid: " + subscriptionId, e);
		}
		
		//TODO: comment below thread before committing, use it in local only as pubsub does not work locally
		/*new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(20000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				while(true) {
					try {
						File file = new File("C:/tmp/pubsub.json");
						if (file.exists()) {
							String pubsubMessage = FileUtils.readFileToString(file, "utf-8");
							processPubsubMessage(pubsubMessage);
						}
						
						Thread.sleep(10000);
					} catch (Exception e) {
						logger.error("Error in thread", e);
					}
				}
			}
		}).start();*/
		
	}
	
	@SuppressWarnings("deprecation")
	public void processPubsubMessage(String pubsubMessage) {
		try {
			JSONObject jsonObject = new JSONObject(pubsubMessage);

			String loanId = null;
			if (jsonObject.has("loanId") && !StringUtils.isEmpty(jsonObject.getString("loanId"))) {
				Authentication.setAuthenticatedUserId("admin");
				loanId = jsonObject.getString("loanId");
				logger.info("Cecking if workflow already started for loanid: " + loanId);
				ProcessInstance processInstance = processInstanceService.getProcessInstanceByBusinessKey(loanId);
				if (processInstance != null) {
					logger.info("Found already running workflow for loanid: " + loanId + " with process id: "+ processInstance.getId());
					if (ProcessInstanceStatus.RUNNING == (processInstance.getStatus())) {
						if (jsonObject.has("docCount") && jsonObject.getInt("docCount") >= 4) {
							Page<Task> page = taskService.getTasks(processInstance.getId(), Pageable.of(0, 10));
							logger.info("Got all documents, Completing task for loanid: " + loanId + " with process id: "+ processInstance.getId());
							for (Task task : page.getContent()) {
								Map<String, Object> variables = new HashMap<>();
								variables.put("loanStatus", "Completed");
								CompleteTaskPayload taskCmd = new CompleteTaskPayload(task.getId(), variables);
								taskService.completeTask(taskCmd);
							}
							logger.info("Ending workflow now for loanid: " + loanId + " with process id: "+ processInstance.getId());
							processInstanceService.suspend(new SuspendProcessPayload(processInstance.getId()));
							
							if (jsonObject.has("documentGuid")) {
								String documentGuid = jsonObject.getString("documentGuid");
								String documentState = jsonObject.has("documentState") ? jsonObject.getString("documentState") : "METADATA_UPLOADED";
								String companyId = jsonObject.has("companyId") ? jsonObject.getString("companyId") : null;
								logger.info("Updating document: " + documentGuid + " with state: " + documentState + " in CDV...");
								spannerApiClient.updateDocumentState(companyId, documentGuid, documentState);
								logger.info("Update complete for document: " + documentGuid + " with state: " + documentState + " in CDV...");
							}
						} else {
							logger.info("Workflow still waiting for some update for loanid: " + loanId + " with process id: "+ processInstance.getId());
						}
					} else {
						logger.info("Invalid loanid: " + loanId + ", workflow already completed for this loanid");
					}
				} else {
					String processDefinitionId = jsonObject.has("processDefinitionId") ? jsonObject.getString("processDefinitionId") : null;
					startMortgageWorkflowForLoanId(loanId, processDefinitionId);
				}
			} else {
				logger.error("Could not start/update workflow, loanid not found/empty in the message");
			}

		} catch (JSONException e) {
			logger.error("Could not start/update workflow, Message is not in json format: " + pubsubMessage, e);
		} catch (Exception e) {
			logger.error("Could not start/update workflow for message" + pubsubMessage, e);
		}
	}

	public ProcessInstance startMortgageWorkflowForLoanId(String loanId, String processDefinitionId) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("loanId", loanId);
		if (StringUtils.isEmpty(processDefinitionId)) {
			processDefinitionId = mortgageProcessDefinitionId;
		}

		logger.info("Starting new workflow for loanid: " + loanId + " with process definition id: " + processDefinitionId);
		StartProcessPayload cmd = new StartProcessPayload(processDefinitionId, null, loanId, variables);
		ProcessInstance processInstance = processInstanceService.startProcess(cmd);

		logger.info("Workflow started for loanid: " + loanId + ", with process id: " + processInstance.getId());
		return processInstance;
	}

	@PreDestroy
	public void stopMessageReciever() {
		// stop receiving messages
		if (subscriber != null) {
			subscriber.stopAsync();
		}
	}

}
