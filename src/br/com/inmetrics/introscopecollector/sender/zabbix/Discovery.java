package br.com.inmetrics.introscopecollector.sender.zabbix;

import br.com.inmetrics.introscopecollector.core.Collector;
import br.com.inmetrics.introscopecollector.core.IntroscopeCollectorJob;
import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils.Constants;

public class Discovery extends IntroscopeCollectorJob {

	@Override
	public void execute() {
		
		String[] agentsServers = resourceUtils.getProperty(Constants.INTROSCOPE_AGENT_NAMES).split(";");

		Collector collector = new Collector(resourceUtils);

		for (String agentServer : agentsServers) {
			((ZabbixQueues) queues).getDiscoveryList().add(collector.collectMetric(agentServer, startTime, stopTime));
		}

	}

}
