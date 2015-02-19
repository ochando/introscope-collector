package br.com.inmetrics.introscopecollector.sender.zabbix;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.inmetrics.introscopecollector.core.IntroscopeSimpleJob;

public class ParserDiscovery extends IntroscopeSimpleJob {

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Override
	public void execute() {
		List<ResultSet> resultSets = new ArrayList<ResultSet>();

		if (!((ZabbixQueues) queues).getDiscoveryList().isEmpty()) {
			try {
				Set<String> keyValues = new HashSet<String>();

				((ZabbixQueues) queues).getDiscoveryList().drainTo(resultSets);

				for (ResultSet resultSet : resultSets) {

					while (resultSet.next()) {
						keyValues.add(resultSet.getString("Host"));
						String metricUnique = new String(resultSet.getString("Resource").replaceAll("[\\_\\-\\|\\@]",
								"."));
						keyValues.add(metricUnique);
					}
					
				}
				((ZabbixQueues) queues).getDiscoveryListOut().addAll(keyValues);
				
			} catch (SQLException e) {
				LOG.error("Error parsing discovery.", e);
			}
		}
	}
}
