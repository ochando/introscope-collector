package br.com.inmetrics.introscopecollector.sender.zabbix;

import java.util.TimerTask;

import br.com.inmetrics.introscopecollector.core.Collector;
import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils;
import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils.Constants;

public class Discovery extends TimerTask {

	private ResourceUtils resourceUtils;
	private ZabbixQueues queues;

	public Discovery(ResourceUtils resourceUtils, ZabbixQueues queues) {
		this.resourceUtils = resourceUtils;
		this.queues = queues;
	}

	@Override
	public void run() {
		Collector collector = new Collector(resourceUtils);
		String[] agentsServers = resourceUtils.getProperty(Constants.INTROSCOPE_AGENT_NAMES).split(";");

		for (String agentServer : agentsServers) {
			queues.getDiscoveryList().add(collector.collectMetric(agentServer));
		}
	}

}
