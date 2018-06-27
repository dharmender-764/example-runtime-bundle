package com.ironmountain.rmaas.activiti.google.pubsub;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;

@Component
public class PubsubPullSubscriber {

	private static final Logger logger = LoggerFactory.getLogger(PubsubPullSubscriber.class);

	@Value("${gcp.projectid}")
	private String projectId;

	@Value("${gcp.pubsub.pull.subscriptionid}")
	private String subscriptionId;

	private Subscriber subscriber = null;

	@PostConstruct
	public void initiateMessageReceiver() {

		logger.info("******** Starting Google pub/sub pull subscriber with project id: " + projectId + " and subid: "
				+ subscriptionId);

		ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId);
		// Instantiate an asynchronous message receiver
		MessageReceiver receiver = new MessageReceiver() {
			@Override
			public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
				// handle incoming message, then ack/nack the received message
				System.out.println("Got pub/sub message:");
				System.out.println("	Id : " + message.getMessageId());
				System.out.println("	Data : " + message.getData().toStringUtf8());

				logger.info("Got pub/sub message with Id: " + message.getMessageId() + " and data: "
						+ message.getData().toStringUtf8());
				consumer.ack();
			}
		};

		try {
			// Create a subscriber for "my-subscription-id" bound to the message receiver
			subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
			subscriber.startAsync();
			
			subscriber.addListener(new Subscriber.Listener() {
				public void failed(Subscriber.State from, Throwable failure) {
					logger.error("error getting pubsub message", failure);
					failure.printStackTrace();
				}
			}, MoreExecutors.directExecutor());
		} catch (Exception e) {
			logger.error("Could not start google pub/sub pull subscriber for project id: " + projectId + " and subid: "
					+ subscriptionId, e);
		}
	}

	@PreDestroy
	public void stopMessageReciever() {
		// stop receiving messages
		if (subscriber != null) {
			subscriber.stopAsync();
		}
	}

}
