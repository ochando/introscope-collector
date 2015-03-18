package br.com.inmetrics.introscopecollector;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.inmetrics.introscopecollector.core.Executor;
import br.com.inmetrics.introscopecollector.sender.Sender;
import br.com.inmetrics.introscopecollector.sender.zabbix.ZabbixQueues;
import br.com.inmetrics.introscopecollector.util.parser.ParserMetricName;
import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils;
import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils.Constants;

public class EntryPoint {

	private static Logger LOG = LoggerFactory.getLogger(EntryPoint.class);

	public static IntroscopeCollector introscopeCollector;

	public static void main(String[] args) {

		try {
			String propertiesFile = args[0];
			ZabbixQueues queues = new ZabbixQueues();

			introscopeCollector = new IntroscopeCollector();
			introscopeCollector.initializeAgent(propertiesFile);

			ResourceUtils resourceUtils = introscopeCollector.getResourceUtils();
			
			Integer collectCron = Integer.valueOf(resourceUtils.getProperty(Constants.COLLECT_CRON_EVERY_MIN));
			Integer discoveryCron = Integer.valueOf(resourceUtils.getProperty(Constants.DISCOVERY_CRON_EVERY_MIN));
			
			if ((collectCron < 0 || collectCron > 59) || (discoveryCron < 0 || discoveryCron > 59)) {
				throw new IllegalArgumentException("Both collect and discovery minutes must be 0-59, in a \"every minute\" behavior.");
			}

			SchedulerFactory schedFact = new StdSchedulerFactory();
			Scheduler sched = schedFact.getScheduler();
			sched.start();

			JobDataMap jobDataMap = new JobDataMap();
			jobDataMap.put("resourceUtils", resourceUtils);
			jobDataMap.put("queues", queues);

			JobDetail executorJob = newJob(Executor.class).withIdentity("ExecutorJob", "executor")
					.usingJobData(jobDataMap).build();
			
			JobDetail parserExecutorJob = newJob(ParserMetricName.class).withIdentity("ParserExecutorJob", "executor")
					.usingJobData(jobDataMap).build();

			Trigger executorTrigger = newTrigger().withIdentity("ExecutorTrigger", "executor")
					.withSchedule(cronSchedule("0 0/" + collectCron + " * * * ?")).build();
			
			Trigger parserExecutorTrigger = newTrigger().withIdentity("ParserExecutorTrigger", "executor")
					.withSchedule(simpleSchedule().withIntervalInMilliseconds(500).repeatForever()).build();

//			JobDetail discoveryJob = newJob(Discovery.class).withIdentity("DiscoveryJob", "discovery")
//					.usingJobData(jobDataMap).build();
			
//			JobDetail parserDiscoveryJob = newJob(ParserDiscovery.class).withIdentity("ParserDiscoveryJob", "discovery")
//					.usingJobData(jobDataMap).build();

//			Trigger discoveryTrigger = newTrigger().withIdentity("DiscoveryTrigger", "discovery")
//					.withSchedule(cronSchedule("0 0/" + discoveryCron + " * * * ?")).build();
			
//			Trigger parserDiscoveryTrigger = newTrigger().withIdentity("ParserDiscoveryTrigger", "discovery")
//					.withSchedule(simpleSchedule().withIntervalInMilliseconds(500).repeatForever()).build();
			
			JobDetail senderJob = newJob(Sender.class).withIdentity("SenderJob", "sender")
					.usingJobData(jobDataMap).build();
			
			Trigger senderTrigger = newTrigger().withIdentity("SenderTrigger", "sender")
					.withSchedule(simpleSchedule().withIntervalInMilliseconds(500).repeatForever()).build();

			sched.scheduleJob(executorJob, executorTrigger);
			sched.scheduleJob(parserExecutorJob, parserExecutorTrigger);
			
//			sched.scheduleJob(discoveryJob, discoveryTrigger);
//			sched.scheduleJob(parserDiscoveryJob, parserDiscoveryTrigger);
			
			sched.scheduleJob(senderJob, senderTrigger);

		} catch (Throwable t) {
			LOG.error("Error in main thread.", t);
		}

	}
}
