package br.com.inmetrics.introscopecollector.util.parser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.inmetrics.introscopecollector.core.IntroscopeSimpleJob;
import br.com.inmetrics.introscopecollector.model.MetricDataBean;
import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils.Constants;

public class ParserMetricName extends IntroscopeSimpleJob {

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	public void execute() {
		LinkedBlockingQueue<ResultSet> inputResult = queues.getResultSets();
		LinkedBlockingQueue<MetricDataBean> outputResult = queues.getOutputQueue();

		List<ResultSet> resultSets = new ArrayList<ResultSet>();

		if (!inputResult.isEmpty()) {
			inputResult.drainTo(resultSets);

			try {
				for (ResultSet resultSet : resultSets) {

					HashMap<String, List<MetricDataBean>> metricsToCalculate = new HashMap<String, List<MetricDataBean>>();

					while (resultSet.next()) {
						MetricDataBean metricDataBean = new MetricDataBean();

						metricDataBean.setHost(resultSet.getString("Host"));
						metricDataBean.setDomain(resultSet.getString("Domain"));
						metricDataBean.setResource(resultSet.getString("Resource").replaceAll("[\\-\\|\\@]", "."));
						metricDataBean.setAgentName(resultSet.getString("AgentName"));
						metricDataBean.setMetricName(resultSet.getString("MetricName"));
						metricDataBean.setValue(resultSet.getString("Value"));
						metricDataBean.setMax(resultSet.getString("Max"));
						metricDataBean.setMin(resultSet.getString("Min"));

						String uniqueKey = metricDataBean.getHost() + metricDataBean.getDomain()
								+ metricDataBean.getAgentName() + metricDataBean.getResource()
								+ metricDataBean.getMetricName();

						if (!metricsToCalculate.containsKey(uniqueKey)) {
							ArrayList<MetricDataBean> list = new ArrayList<MetricDataBean>();
							list.add(metricDataBean);
							metricsToCalculate.put(uniqueKey, list);
						} else {
							metricsToCalculate.get(uniqueKey).add(metricDataBean);
						}

						if (resourceUtils.getProperty(Constants.INTROSCOPE_LIST_METRICS).equals("true")) {
							LOG.info("Hostname: " + metricDataBean.getHost());
							LOG.info("Domain: " + metricDataBean.getDomain());
							LOG.info("Resource: " + metricDataBean.getResource());
							LOG.info("Agent: " + metricDataBean.getAgentName());
							LOG.info("Metric: " + metricDataBean.getMetricName() + "\n");
						}

					}

					if (resourceUtils.getProperty(Constants.INTROSCOPE_LIST_METRICS).equals("true")) {
						System.exit(0);
					}

					List<MetricDataBean> metricsToSend = new ArrayList<MetricDataBean>();

					for (String uniq : metricsToCalculate.keySet()) {
						List<MetricDataBean> list = metricsToCalculate.get(uniq);
						String[] listAvg = resourceUtils.getProperty(Constants.INTROSCOPE_METRICS_AGGREGATOR_AVG)
								.split(";");
						String[] listSum = resourceUtils.getProperty(Constants.INTROSCOPE_METRICS_AGGREGATOR_SUM)
								.split(";");
						for (String metricAvg : listAvg) {
							if (uniq.contains(metricAvg)) {
								Long sum = 0L;
								for (MetricDataBean metricDataBean : list) {
									sum += Long.valueOf(metricDataBean.getValue());
								}
								Long avg = sum / list.size();

								MetricDataBean beanToSend = list.get(0);
								beanToSend.setValue(avg.toString());

								metricsToSend.add(beanToSend);
							}
						}
						for (String metricSum : listSum) {
							if (uniq.contains(metricSum)) {
								Long sum = 0L;
								for (MetricDataBean metricDataBean : list) {
									sum += Long.valueOf(metricDataBean.getValue());
								}
								MetricDataBean beanToSend = list.get(0);
								beanToSend.setValue(sum.toString());

								metricsToSend.add(beanToSend);
							}
						}

					}

					outputResult.addAll(metricsToSend);

				}

			} catch (SQLException e) {
				LOG.error("Error parsing metrics.", e);
			}

		}
	}
}
