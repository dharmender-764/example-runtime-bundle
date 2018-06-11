package org.activiti.cloud.runtime;

import java.io.IOException;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

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
		//zorroaClient.performEmptySearch();
		
		int totalCount = 0;
		try {
			Map<String, Map<String, Object>> result = zorroaClient.performTermSearchRawJson();
			totalCount = (int) result.get("page").get("totalCount");
			System.out.println("Got #" + totalCount + " documents from zorroa search");
		} catch (IOException e) {
			logger.error("Error performing zorroa search", e);
		}
		execution.setVariable("resultCount", totalCount);
	}

	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		System.out.println("*************************ZorroaDelegate setApplicationContext called ************************");
		ZorroaDelegate.context = context;
	}
}
