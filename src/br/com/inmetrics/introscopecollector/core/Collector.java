package br.com.inmetrics.introscopecollector.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils;
import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils.Constants;

public class Collector {

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	private final ResourceUtils resourceUtils;

	public Collector(ResourceUtils resourceUtils) {
		this.resourceUtils = resourceUtils;
	}

	public ResourceUtils getResourceUtils() {
		return resourceUtils;
	}

	public ResultSet collectMetric(String introscopeAgent, Date startTime, Date stopTime) {

		Connection conn = null;

		try {

			SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);

			Class.forName("com.wily.introscope.jdbc.IntroscopeDriver");

			conn = DriverManager.getConnection("jdbc:introscope:net//"
					+ resourceUtils.getProperty(Constants.INTROSCOPE_USER) + ":"
					+ resourceUtils.getProperty(Constants.INTROSCOPE_PASS) + "@"
					+ resourceUtils.getProperty(Constants.INTROSCOPE_SERVER) + ":"
					+ resourceUtils.getProperty(Constants.INTROSCOPE_EM_PORT));

			ResultSet resultSet = selectMetrics(conn, dateFormat.format(startTime), dateFormat.format(stopTime),
					introscopeAgent);

			return resultSet;
		} catch (Exception e) {
			LOG.error("Error collecting metrics.", e);
			return null;
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				LOG.error("Error closing connection.", e);
			}
		}

	}

	public ResultSet selectMetrics(Connection conn, String startTime, String stopTime, String introscopeAgent) {

		Statement stmt = null;
		String selectStatement = "select * from metric_data where agent='" + introscopeAgent + "' and metric='"
				+ resourceUtils.getProperty(Constants.INTROSCOPE_NODE_START) + "' and timestamp between '" + startTime
				+ "' and '" + stopTime + "' order by timestamp ";

		LOG.debug(selectStatement);

		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(selectStatement);

		} catch (SQLException e) {
			LOG.error("Error selecting metrics.", e);
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				LOG.error("Error closing connection", e);
			}
		}

		return rs;

	}
}
