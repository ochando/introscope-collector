package br.com.inmetrics.introscopecollector.util.parser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.inmetrics.introscopecollector.model.MetricDataBean;
import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils;
import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils.Constants;
import br.com.inmetrics.introscopecollector.util.queue.Queues;

public class ParserMetricName implements Runnable {

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	private LinkedBlockingQueue<ResultSet> inputResult;
	private LinkedBlockingQueue<MetricDataBean> outputResult;
	private Queues queues;
	private ResourceUtils resourceUtils;

	public ParserMetricName(Queues queues, ResourceUtils resourceUtils) {
		this.queues = queues;
		this.inputResult = this.queues.getResultSets();
		this.outputResult = this.queues.getOutputQueue();
		this.resourceUtils = resourceUtils;
	}

	public void run() {
		ResultSet resultSet;

		while (true) {

			if (!this.inputResult.isEmpty()) {
				resultSet = (ResultSet) inputResult.poll();

				try {
					if (resultSet != null) {

						HashMap<String, List<MetricDataBean>> metricsToCalculate = new HashMap<String, List<MetricDataBean>>();

						while (resultSet.next()) {
							MetricDataBean metricDataBean = new MetricDataBean();

							metricDataBean.setHost(resultSet.getString("Host"));
							metricDataBean.setDomain(resultSet.getString("Domain"));
							metricDataBean.setResource(resultSet.getString("Resource")
									.replaceAll("[\\_\\-\\|\\@]", "."));
							metricDataBean.setAgentName(resultSet.getString("Agentname"));
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
								LOG.info("Resource: " + resultSet.getString("Resource"));
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

						this.outputResult.addAll(metricsToSend);

					}

				} catch (SQLException e) {
					LOG.error("Error parsing metrics.", e);
				}

			} else {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					LOG.error("Error sleeping while queue is empty.", e);
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				LOG.error("Error in parse looping.", e);
			}
		}
	}
}
