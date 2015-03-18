package br.com.inmetrics.introscopecollector.util.properties;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceUtils {

	private Logger LOG = LoggerFactory.getLogger(this.getClass());
	
	private Properties properties;

	public ResourceUtils(String propertiesFile) {
		properties = new Properties();

		FileInputStream in;
		try {
			in = new FileInputStream(new File(propertiesFile));
			properties.load(in);
		} catch (Exception e) {
			LOG.error("Error loading properties.", e);
		}
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public static class Constants {

		public static final String INTROSCOPE_SERVER = "introscope.server";
		public static final String INTROSCOPE_EM_PORT = "intrscope.em.port";
		public static final String INTROSCOPE_USER = "introscope.user";
		public static final String INTROSCOPE_PASS = "introscope.pass";
		public static final String INTROSCOPE_AGENT_NAMES = "introscope.agent.names";
		public static final String INTROSCOPE_NODE_START = "introscope.node.start";
		public static final String INTROSCOPE_METRICS_AGGREGATOR_SUM = "introscope.metrics.aggregator.sum";
		public static final String INTROSCOPE_METRICS_AGGREGATOR_AVG = "introscope.metrics.aggregator.avg";

		public static final String COLLECT_CRON_EVERY_MIN = "collect.cron.every.min";
		public static final String DISCOVERY_CRON_EVERY_MIN = "discovery.cron.every.min";

		public static final String DATE_FORMAT = "MM/dd/yy HH:mm:ss";

		public static final String ZABBIX_SERVER = "zabbix.server";
		public static final String ZABBIX_PORT = "zabbix.port";

		public static final String INTROSCOPE_LIST_METRICS = "list.metrics.exit";
		public static final String DEBUG = "introscope.collector.debug";
		
		public static final String KAFKA_BROKER_LIST = "kafka.broker.list";
		public static final String KAFKA_REQUIRED_ACKS = "kafka.required.acks";
		public static final String KAFKA_TOPIC_NAME = "kafka.topic.name";
	}

}
