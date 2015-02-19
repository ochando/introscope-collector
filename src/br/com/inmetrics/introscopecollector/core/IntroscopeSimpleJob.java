package br.com.inmetrics.introscopecollector.core;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils;
import br.com.inmetrics.introscopecollector.util.queue.Queues;

public abstract class IntroscopeSimpleJob implements Job {

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	protected ResourceUtils resourceUtils;
	protected Queues queues;
	
	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		try {
			resourceUtils = (ResourceUtils) jobExecutionContext.getJobDetail().getJobDataMap().get("resourceUtils");
			queues = (Queues) jobExecutionContext.getJobDetail().getJobDataMap().get("queues");
			
			execute();
		} catch (Exception e) {
			LOG.error("Error in executor.", e);
		}
	}
	
	public abstract void execute();

}
