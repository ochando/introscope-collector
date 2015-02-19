package br.com.inmetrics.introscopecollector.core;

import java.util.Calendar;
import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils;
import br.com.inmetrics.introscopecollector.util.queue.Queues;

public abstract class IntroscopeCollectorJob implements Job {

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	protected ResourceUtils resourceUtils;
	protected Queues queues;
	protected Date startTime;
	protected Date stopTime;

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		try {
			resourceUtils = (ResourceUtils) jobExecutionContext.getJobDetail().getJobDataMap().get("resourceUtils");
			queues = (Queues) jobExecutionContext.getJobDetail().getJobDataMap().get("queues");

			startTime = null;
			if (jobExecutionContext.getPreviousFireTime() != null) {
				startTime = plusOneSecond(jobExecutionContext.getPreviousFireTime());
			} else
				return;

			//Waits one minuto for Introscope
			startTime = minusOneMinute(startTime);
			stopTime = minusOneMinute(jobExecutionContext.getFireTime());

			execute();

		} catch (Exception e) {
			LOG.error("Error in executor.", e);
		}
	}

	private Date plusOneSecond(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.SECOND, 1);
		return calendar.getTime();
	}
	
	private Date minusOneMinute(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, -1);
		return calendar.getTime();
	}
	
	public abstract void execute();
}
