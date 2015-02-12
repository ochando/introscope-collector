package br.com.inmetrics.introscopecollector.sender.kafka;

import java.util.List;

import br.com.inmetrics.introscopecollector.model.MetricDataBean;
import br.com.inmetrics.introscopecollector.sender.ISender;
import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils;
import br.com.inmetrics.introscopecollector.util.queue.Queues;

public class KafkaSender extends ISender {

	public KafkaSender(Queues queues, ResourceUtils resourceUtils) {
		super(queues, resourceUtils);
		
	}

	public void send(List<MetricDataBean> metricDataBeans) {
		
	}

}
