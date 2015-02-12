package br.com.inmetrics.introscopecollector.core;

import java.util.TimerTask;

import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils;
import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils.Constants;
import br.com.inmetrics.introscopecollector.util.queue.Queues;

public class Executor extends TimerTask {

	private ResourceUtils resourceUtils;
	private Queues queues;

	public Executor(ResourceUtils resourceUtils, Queues queues) {
		this.resourceUtils = resourceUtils;
		this.queues = queues;
	}

	@Override
	public void run() {
		Collector collector = new Collector(resourceUtils);
		String[] agentsServers = resourceUtils.getProperty(Constants.INTROSCOPE_AGENT_NAMES).split(";");
		for (String agentServer : agentsServers)
			queues.getResultSets().add(collector.collectMetric(agentServer));
	}

}
