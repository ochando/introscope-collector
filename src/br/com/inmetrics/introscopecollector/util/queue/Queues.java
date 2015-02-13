package br.com.inmetrics.introscopecollector.util.queue;

import java.sql.ResultSet;
import java.util.concurrent.LinkedBlockingQueue;

import br.com.inmetrics.introscopecollector.model.MetricDataBean;

public class Queues {

	private LinkedBlockingQueue<ResultSet> resultSets;
	private LinkedBlockingQueue<MetricDataBean> outputQueue;

	public Queues() {
		this.resultSets = new LinkedBlockingQueue<ResultSet>();
		this.outputQueue = new LinkedBlockingQueue<MetricDataBean>();
	}

	public LinkedBlockingQueue<ResultSet> getResultSets() {
		return resultSets;
	}

	public void setResultSets(LinkedBlockingQueue<ResultSet> resultSets) {
		this.resultSets = resultSets;
	}

	public LinkedBlockingQueue<MetricDataBean> getOutputQueue() {
		return outputQueue;
	}

	public void setOutputQueue(LinkedBlockingQueue<MetricDataBean> outputQueue) {
		this.outputQueue = outputQueue;
	}

}
