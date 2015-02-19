package br.com.inmetrics.introscopecollector.core;

import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils.Constants;

public class Executor extends IntroscopeCollectorJob {

	@Override
	public void execute() {

		String[] agentsServers = resourceUtils.getProperty(Constants.INTROSCOPE_AGENT_NAMES).split(";");

		Collector collector = new Collector(resourceUtils);

		for (String agentServer : agentsServers) {
			queues.getResultSets().add(collector.collectMetric(agentServer, startTime, stopTime));
		}

	}
}
