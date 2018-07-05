package com.ironmountain.rmaas.activiti.zorroa;

import java.io.IOException;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

import com.ironmountain.rmaas.activiti.zorroa.model.Clip;
import com.ironmountain.rmaas.activiti.zorroa.model.Document;
import com.ironmountain.rmaas.activiti.zorroa.model.Media;
import com.ironmountain.rmaas.activiti.zorroa.model.Source;
import com.ironmountain.rmaas.activiti.zorroa.model.ZorroaSearchResponse;

@Configuration
public class ZorroaDelegate implements JavaDelegate, ApplicationContextAware {

	private static final Logger logger = LoggerFactory.getLogger(ZorroaDelegate.class);

	private static ApplicationContext context;
	
	public void execute(DelegateExecution execution) {
		System.out.println("execution id " + execution.getId());
		Object loanId = execution.getVariable("loanId");
		System.out.println("Performing Zorroa search for loan id: " + loanId);
		System.out.println("context = " + context);
		ZorroaClient zorroaClient = context.getBean("zorroaClient", ZorroaClient.class);
		
		int totalCount = 0;
		try {
			ZorroaSearchResponse searchResponse = zorroaClient.performSearch();
			totalCount = searchResponse.getPage().getTotalCount();
			logDocuments(searchResponse);
		} catch (IOException e) {
			logger.error("Error performing zorroa search" + e);
		}
		execution.setVariable("docCount", totalCount);
	}

	public void logDocuments(ZorroaSearchResponse searchResponse) {
		System.out.println("\nTotal docs: " + searchResponse.getPage().getTotalCount());
		System.out.println("Document details:");
		for (Document document : searchResponse.getDocuments()) {
			System.out.println("##########################Document#########################");
			System.out.println("\tId: " + document.getId());
			System.out.println("\tType: " + document.getType());
			System.out.println("\tScore: " + document.getScore());

			Source source = document.getDocument().getSource();
			if (source != null) {
				System.out.println("\tSource details:");
				System.out.println("\t\tFile Name: " + source.getFilename());
				System.out.println("\t\tFile Size: " + source.getFileSize());
				System.out.println("\t\tBase Name: " + source.getBasename());
				System.out.println("\t\tExtension: " + source.getExtension());
				System.out.println("\t\tCreated time: " + source.getTimeCreated());
				System.out.println("\t\tPath: " + source.getPath());
			}
			
			Media media = document.getDocument().getMedia();
			if (media != null) {
				System.out.println("\tMedia details:");
				System.out.println("\t\tAuthor: " + media.getAuthor());
				System.out.println("\t\tPages: " + media.getPages());
				System.out.println("\t\tCreated time: " + media.getTimeCreated());
				
				Clip clip = media.getClip();
				if (clip != null) {
					System.out.println("\t\tClip Details:");
					System.out.println("\t\t\tStart: " + clip.getStart());
					System.out.println("\t\t\tStop: " + clip.getStop());
					System.out.println("\t\t\tLength: " + clip.getLength());
				}
			}
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		System.out.println("*************************ZorroaDelegate setApplicationContext called ************************");
		ZorroaDelegate.context = context;
	}
}
