package br.com.inmetrics.introscopecollector;

import java.util.Timer;
import java.util.TimerTask;

import br.com.inmetrics.introscopecollector.core.Executor;
import br.com.inmetrics.introscopecollector.sender.Sender;
import br.com.inmetrics.introscopecollector.sender.zabbix.Discovery;
import br.com.inmetrics.introscopecollector.sender.zabbix.ParserDiscovery;
import br.com.inmetrics.introscopecollector.sender.zabbix.ZabbixQueues;
import br.com.inmetrics.introscopecollector.util.parser.ParserMetricName;
import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils.Constants;

public class EntryPoint {

	public static IntroscopeCollector introscopeCollector;

	public static void main(String[] args) {

		String propertiesFile = args[0];
		ZabbixQueues queues = new ZabbixQueues();
		ParserMetricName parser;
		Sender sender;
		ParserDiscovery parserDiscovery;
		TimerTask executor;
		TimerTask discovery;
		Timer timer = new Timer();
		;
		final Thread parserThread;
		final Thread senderThread;
		final Thread discoveryThread;

		introscopeCollector = new IntroscopeCollector();
		introscopeCollector.initializeAgent(propertiesFile);

		parser = new ParserMetricName(queues, introscopeCollector.getResourceUtils());

		parserDiscovery = new ParserDiscovery(queues, introscopeCollector.getResourceUtils());

		executor = new Executor(introscopeCollector.getResourceUtils(), queues);
		discovery = new Discovery(introscopeCollector.getResourceUtils(), queues);

		timer.schedule(executor, 5000,
				Integer.valueOf(introscopeCollector.getResourceUtils().getProperty(Constants.COLLECT_INTERVAL)) * 1000);

		timer.schedule(
				discovery,
				5000,
				Integer.valueOf(introscopeCollector.getResourceUtils().getProperty(Constants.DISCOVERY_INTERVAL)) * 1000);

		sender = new Sender(queues, introscopeCollector.getResourceUtils());

		parserThread = new Thread(parser, "Parser");
		parserThread.start();

		senderThread = new Thread(sender, "Sender");
		senderThread.start();

		discoveryThread = new Thread(parserDiscovery, "Discovery");
		discoveryThread.start();

	}

}
