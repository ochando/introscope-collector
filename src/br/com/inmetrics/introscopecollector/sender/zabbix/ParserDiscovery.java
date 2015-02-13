package br.com.inmetrics.introscopecollector.sender.zabbix;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils;

public class ParserDiscovery implements Runnable {

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	private ZabbixQueues queues;

	public ParserDiscovery(ZabbixQueues queues, ResourceUtils resourceUtils) {
		this.queues = queues;
	}

	@Override
	public void run() {
		ResultSet resultSet;

		while (true) {
			if (!queues.getDiscoveryList().isEmpty()) {
				try {
					Set<String> keyValues = new HashSet<String>();

					resultSet = (ResultSet) queues.getDiscoveryList().poll();

					if (resultSet != null) {

						while (resultSet.next()) {
							keyValues.add(resultSet.getString("Host"));
							String metricUnique = new String(resultSet.getString("Resource").replaceAll(
									"[\\_\\-\\|\\@]", "."));
							keyValues.add(metricUnique);
						}

						queues.getDiscoveryListOut().addAll(keyValues);
					}
				} catch (SQLException e) {
					LOG.error("Error parsing discovery.", e);
				}
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				LOG.error("Error in discovery parse loop.", e);
			}
		}

	}
}
