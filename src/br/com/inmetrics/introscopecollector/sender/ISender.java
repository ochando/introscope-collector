package br.com.inmetrics.introscopecollector.sender;

import java.util.List;

import br.com.inmetrics.introscopecollector.model.MetricDataBean;
import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils;
import br.com.inmetrics.introscopecollector.util.queue.Queues;

public abstract class ISender {

	protected Queues queues;
	protected ResourceUtils resourceUtils;
	
	public ISender(Queues queues, ResourceUtils resourceUtils) {
		this.queues = queues;
		this.resourceUtils = resourceUtils;
	}
	
	protected abstract void send(List<MetricDataBean> metricDataBeans);
}
