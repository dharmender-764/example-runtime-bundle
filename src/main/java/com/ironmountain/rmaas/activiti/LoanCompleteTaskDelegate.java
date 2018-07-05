package com.ironmountain.rmaas.activiti;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class LoanCompleteTaskDelegate implements TaskListener {

	private static final Logger logger = LoggerFactory.getLogger(LoanCompleteTaskDelegate.class);

	@Override
	public void notify(DelegateTask delegateTask) {
		DelegateExecution execution = delegateTask.getExecution();
		
		String loanStatus = (String) delegateTask.getVariable("loanStatus");
		
		logger.info("LoanCompleteTaskDelegate execution id " + execution.getId() + ", loanStatus: " + loanStatus);
		Object loanId = execution.getVariable("loanId");
		loanStatus = StringUtils.isEmpty(loanStatus) ? (String) execution.getVariable("loanStatus") : loanStatus;
		logger.info("Mortgage workflow completed with status: " + loanStatus + " for loanid: " + loanId);
	}

}
