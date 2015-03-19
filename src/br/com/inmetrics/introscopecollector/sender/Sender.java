package br.com.inmetrics.introscopecollector.sender;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.inmetrics.introscopecollector.core.IntroscopeSimpleJob;
import br.com.inmetrics.introscopecollector.model.MetricDataBean;
import br.com.inmetrics.introscopecollector.sender.kafka.KafkaSender;

public class Sender extends IntroscopeSimpleJob {

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	public void execute() {

		LinkedBlockingQueue<MetricDataBean> outputResult = queues.getOutputQueue();
		ISender sender = new KafkaSender(queues, resourceUtils);

		try {
			if (!outputResult.isEmpty()) {
				ArrayList<MetricDataBean> metricDataBeans = new ArrayList<MetricDataBean>();
				outputResult.drainTo(metricDataBeans);

				sender.send(metricDataBeans);
			}

		} catch (Exception e) {
			LOG.error("Error in sender metrics", e);
		} finally {
			sender.close();
		}
	}
}
