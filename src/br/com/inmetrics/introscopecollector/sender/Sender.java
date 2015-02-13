package br.com.inmetrics.introscopecollector.sender;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.inmetrics.introscopecollector.model.MetricDataBean;
import br.com.inmetrics.introscopecollector.sender.zabbix.ZabbixSender;
import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils;
import br.com.inmetrics.introscopecollector.util.queue.Queues;

public class Sender implements Runnable {

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	private Queues queues;
	private ResourceUtils resourceUtils;

	public Sender(Queues queues, ResourceUtils resourceUtils) {
		this.queues = queues;
		this.resourceUtils = resourceUtils;
	}

	public void run() {

		LinkedBlockingQueue<MetricDataBean> outputResult = this.queues.getOutputQueue();
		ISender sender = new ZabbixSender(queues, resourceUtils);

		while (true) {

			try {
				if (!outputResult.isEmpty()) {
					ArrayList<MetricDataBean> metricDataBeans = new ArrayList<MetricDataBean>();
					outputResult.drainTo(metricDataBeans);

					sender.send(metricDataBeans);
				}

				Thread.sleep(100);
				
			} catch (Exception e) {
				LOG.error("Error in sender metrics", e);
			}
		}

	}
}
