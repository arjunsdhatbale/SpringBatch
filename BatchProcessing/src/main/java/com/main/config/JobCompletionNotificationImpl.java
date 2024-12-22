package com.main.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Timeouts;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionNotificationImpl implements JobExecutionListener{

	
	private Logger logger = LoggerFactory.getLogger(JobCompletionNotificationImpl.class);
	
	private static final long TIMEOUT  = 10000L; 
	
	@Override
	public void beforeJob(JobExecution jobExecution) {
 
		long startTime = System.currentTimeMillis()	;
		jobExecution.getExecutionContext().put("startTime", startTime);
		
		logger.info("Job started at : " + startTime);
		JobExecutionListener.super.beforeJob(jobExecution);
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		
		
		if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
			logger.info("Job Completed...");
		}
		
		long startTime = jobExecution.getExecutionContext().getLong("startTime",0);
		long duration = System.currentTimeMillis() - startTime; 
		
		
//		if(jobExecution.getStatus() == BatchStatus.STARTED || jobExecution.getStatus() == BatchStatus.UNKNOWN) {
//			try {
//				
//			} catch (Exception e) {
//				// TODO: handle exception
//			}
//		}
		if(duration > TIMEOUT) {
			
			logger.error("Job execution exceed the time out limit of " + TIMEOUT  + " ms. Job stopped.");
			jobExecution.setStatus(BatchStatus.FAILED);
			jobExecution.setStatus(BatchStatus.STOPPED);
			
		}else {
			if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
				logger.info("Job Completed");
			}
			JobExecutionListener.super.afterJob(jobExecution);
		}
		
		
 		
	}

	
	
	
}
