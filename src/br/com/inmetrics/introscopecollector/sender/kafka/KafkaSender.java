package br.com.inmetrics.introscopecollector.sender.kafka;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.inmetrics.introscopecollector.model.MetricDataBean;
import br.com.inmetrics.introscopecollector.sender.ISender;
import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils;
import br.com.inmetrics.introscopecollector.util.queue.Queues;
import br.com.produban.openbus.avro.AvroEncoder;
import br.com.produban.openbus.model.avro.ZabbixAgentData;

public class KafkaSender extends ISender {

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	private Producer<String, byte[]> producer;

	private final String brokerList;
	private final boolean requiredAcks;
	private final String topicName;

	public KafkaSender(Queues queues, ResourceUtils resourceUtils) {
		super(queues, resourceUtils);

		brokerList = resourceUtils.getProperty(ResourceUtils.Constants.KAFKA_BROKER_LIST);
		requiredAcks = Boolean.valueOf(resourceUtils.getProperty(ResourceUtils.Constants.KAFKA_REQUIRED_ACKS));
		topicName = resourceUtils.getProperty(ResourceUtils.Constants.KAFKA_TOPIC_NAME);

		Properties props = new Properties();
		props.put("metadata.broker.list", brokerList);
		props.put("request.required.acks", requiredAcks ? "1" : "0");
		props.put("serializer.class", "kafka.serializer.DefaultEncoder");

		producer = new Producer<String, byte[]>(new ProducerConfig(props));
	}

	public void send(List<MetricDataBean> metricDataBeans) {
		try {
			if (metricDataBeans.size() > 0)
				LOG.debug(Thread.currentThread().getName() + " processing " + metricDataBeans.size() + " messages.");
			List<KeyedMessage<String, byte[]>> keyedMessages = new ArrayList<KeyedMessage<String, byte[]>>();
			for (MetricDataBean metricDataBean : metricDataBeans) {
				ZabbixAgentData agentData = new ZabbixAgentData();
				agentData.setHost(metricDataBean.getHost());
				agentData.setKey("introscope[\"" + metricDataBean.getResource() + "," + metricDataBean.getMetricName()
						+ "\"]");
				agentData.setValue(metricDataBean.getValue());
				String nanoTime = String.valueOf(new Date().getTime());
				String clock = nanoTime.substring(0,10);
				String ns = nanoTime.substring(10,nanoTime.length());
				agentData.setClock(clock);
				agentData.setNs(ns);
				keyedMessages.add(new KeyedMessage<String, byte[]>(topicName, AvroEncoder.toByteArray(agentData)));
			}
			producer.send(keyedMessages);
		} catch (Exception e) {
			LOG.error("Error in kafka sender.", e);
		} 
	}

	@Override
	protected void close() {
		producer.close();
	}

}
