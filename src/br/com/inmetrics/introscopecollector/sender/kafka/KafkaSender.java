package br.com.inmetrics.introscopecollector.sender.kafka;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.inmetrics.introscopecollector.model.MetricDataBean;
import br.com.inmetrics.introscopecollector.sender.ISender;
import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils;
import br.com.inmetrics.introscopecollector.util.queue.Queues;

public class KafkaSender extends ISender {

	private Logger LOG = LoggerFactory.getLogger(this.getClass());
	
	public KafkaSender(Queues queues, ResourceUtils resourceUtils) {
		super(queues, resourceUtils);
		
	}

	public void send(List<MetricDataBean> metricDataBeans) {
		try {
			
		} catch (Exception e) {
			LOG.error("Error in kafka sender.", e);
		}
	}

}
