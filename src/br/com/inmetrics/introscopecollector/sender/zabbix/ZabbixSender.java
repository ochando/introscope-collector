package br.com.inmetrics.introscopecollector.sender.zabbix;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.inmetrics.introscopecollector.model.MetricDataBean;
import br.com.inmetrics.introscopecollector.sender.ISender;
import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils;
import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils.Constants;
import br.com.inmetrics.introscopecollector.util.queue.Queues;

public class ZabbixSender extends ISender {

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	public static final int DEFAULT_PORT = 10051;
	public static final byte[] ZABBIX_HEADER = new byte[] { 'Z', 'B', 'X', 'D', '\1', };

	protected String zabbixServer;
	protected int port = DEFAULT_PORT;

	private Socket sock = null;

	public ZabbixSender(Queues queues, ResourceUtils resourceUtils) {
		super(queues, resourceUtils);
		setZabbixServer(resourceUtils.getProperty(Constants.ZABBIX_SERVER));
		setPort(Integer.valueOf(resourceUtils.getProperty(Constants.ZABBIX_PORT)));
	}

	public void setZabbixServer(String zabbixServer) {
		this.zabbixServer = zabbixServer;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public ZabbixSenderResponse sendItems(ArrayList<ZabbixSenderItem> items) throws IOException {

		JSONObject requestObj = new JSONObject();
		requestObj.put("request", "sender data");
		requestObj.put("data", items);
		return send(requestObj.toString());
	}

	public ZabbixSenderResponse sendItems(ZabbixSenderItem... items) throws IOException {

		JSONObject requestObj = new JSONObject();
		requestObj.put("request", "sender data");
		requestObj.put("data", items);
		return send(requestObj.toString());
	}

	public String sendItemsDiscovery(ArrayList<ZabbixSenderItemDiscovery> items) {
		StringBuilder jsonData = new StringBuilder();
		jsonData.append("{\n\t\"data\":[");

		for (ZabbixSenderItemDiscovery item : items) {
			jsonData.append("\n\t\t{\n\t\t\t\"{#SERVICE}\":\"" + item.getValue() + "\"},");
		}
		jsonData.deleteCharAt(jsonData.length() - 1);
		jsonData.append("]}");

		return jsonData.toString();
	}

	protected ZabbixSenderResponse send(String jsonMessage) throws IOException {
		byte[] data = jsonMessage.getBytes("utf-8");
		int v = data.length;

		sock = new Socket();
		sock.setSoTimeout(30000);
		sock.connect(new InetSocketAddress(zabbixServer, port), 30000);

		OutputStream out = sock.getOutputStream();
		InputStream in = sock.getInputStream();

		byte len0 = (byte) ((0) & 0xFF);
		byte len1 = (byte) ((0) & 0xFF);
		byte len2 = (byte) ((0) & 0xFF);
		byte len3 = (byte) ((0) & 0xFF);
		byte len4 = (byte) ((v >> 24) & 0xFF);
		byte len5 = (byte) ((v >> 16) & 0xFF);
		byte len6 = (byte) ((v >> 8) & 0xFF);
		byte len7 = (byte) ((v >> 0) & 0xFF);

		byte[] messageBytes = new byte[v + 13];

		messageBytes[0] = ZABBIX_HEADER[0];
		messageBytes[1] = ZABBIX_HEADER[1];
		messageBytes[2] = ZABBIX_HEADER[2];
		messageBytes[3] = ZABBIX_HEADER[3];
		messageBytes[4] = ZABBIX_HEADER[4];
		messageBytes[5] = len7;
		messageBytes[6] = len6;
		messageBytes[7] = len5;
		messageBytes[8] = len4;
		messageBytes[9] = len3;
		messageBytes[10] = len2;
		messageBytes[11] = len1;
		messageBytes[12] = len0;

		System.arraycopy(data, 0, messageBytes, 13, v);

		// StringBuffer sb = new StringBuffer();
		//
		// for (int i = 0; i < messageBytes.length; i++) {
		//
		// char byte1 = (char) (messageBytes[i]);
		// if (byte1 < 0) {
		// byte1 += 128;
		// }
		// int hi = (int) ((byte1 & 0xF0) >> 4);
		// int lo = (int) (byte1 & 0x0F);
		//
		// char chi;
		// char clo;
		//
		// if (hi < 10) {
		// chi = (char) (48 + hi);
		// } else {
		// chi = (char) (55 + hi);
		// }
		//
		// if (lo < 10) {
		// clo = (char) (48 + lo);
		// } else {
		// clo = (char) (55 + lo);
		// }
		// sb.append(chi).append(clo);
		// }
		//
		// System.out.println(sb.toString());

		out.write(messageBytes);
		out.flush();

		for (byte headerByte : ZABBIX_HEADER) {
			if (in.read() != headerByte)
				throw new IOException("Received invalid zabbix-header");
		}
		byte[] readBuffer = new byte[8];
		if ((in.read(readBuffer)) != 8)
			throw new IOException("Received invalid zabbix-header");

		long messageLength = (((long) readBuffer[7] << 56) + ((long) (readBuffer[6] & 255) << 48)
				+ ((long) (readBuffer[5] & 255) << 40) + ((long) (readBuffer[4] & 255) << 32)
				+ ((long) (readBuffer[3] & 255) << 24) + ((readBuffer[2] & 255) << 16) + ((readBuffer[1] & 255) << 8) + ((readBuffer[0] & 255) << 0));

		if (messageLength < 0 || messageLength > 65535)
			throw new IOException("Received invalid zabbix-header (message length: " + messageLength + ")");

		byte[] message = new byte[(int) messageLength];
		if ((in.read(message)) != messageLength)
			throw new IOException("Received invalid zabbix message (message too short)");

		JSONObject responseObj = new JSONObject(new String(message, "utf-8"));

		return new ZabbixSenderResponse(responseObj.getString("response"), responseObj.getString("info"));

	}

	public static class ZabbixSenderResponse {
		public ZabbixSenderResponse() {
		}

		public ZabbixSenderResponse(String response, String info) {
			this.response = response;
			this.info = info;
		}

		public String response;
		public String info;

		@Override
		public String toString() {
			return response + ": " + info;
		}

	}

	public static class ZabbixSenderItemDiscovery {
		private String key;
		private String value;

		public ZabbixSenderItemDiscovery(String key, String value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

	}

	public static class ZabbixSenderItem {
		public ZabbixSenderItem() {
		}

		public ZabbixSenderItem(String host, String key, String value) {
			this.host = host;
			this.key = key;
			this.value = value;
		}

		protected String host;
		protected String key;
		protected String value;

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return "ZabbixSenderItem [host=" + host + ", key=" + key + ", value=" + value + "]";
		}

	}

	public void send(List<MetricDataBean> metricDataBeans) {
		ZabbixSenderResponse response = null;
		// ArrayList<ZabbixSenderItemDiscovery> itemDiscoveries;
		// ZabbixSenderItemDiscovery senderItemDiscovery;

		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat(Constants.DATE_FORMAT);
		ArrayList<ZabbixSenderItem> senderItens = new ArrayList<>();

		Set<String> hosts = new HashSet<String>();

		for (MetricDataBean metricDataBean : metricDataBeans) {
			hosts.add(metricDataBean.getHost());

			ZabbixSenderItem senderItem = new ZabbixSenderItem(metricDataBean.getHost(), "introscope[\""
					+ metricDataBean.getResource() + "," + metricDataBean.getMetricName() + "\"]",
					metricDataBean.getValue());
			senderItens.add(senderItem);
		}

		for (String host : hosts) {
			ZabbixSenderItem senderItem = new ZabbixSenderItem(host, "introscope.ping", "1");
			senderItens.add(senderItem);
		}

		try {
			response = this.sendItems(senderItens);
			LOG.info(format.format(date) + ": " + response.toString());
		} catch (IOException e) {
			LOG.error("Error sending itens.", e);
		}

		// TODO Implement DiscoveryZabbix for multiple agents.
		if (!((ZabbixQueues) queues).getDiscoveryListOut().isEmpty()) {
			ArrayList<String> newKeys = new ArrayList<>();
			((ZabbixQueues) queues).getDiscoveryListOut().drainTo(newKeys);
			// itemDiscoveries = new ArrayList<>();
			//
			// int count = 0;
			// String hostDiscovery = "";
			//
			// for (String value : newKeys) {
			// if (count == 0) {
			// hostDiscovery = value;
			// count++;
			// } else {
			// senderItemDiscovery = new ZabbixSenderItemDiscovery(
			// "{#SERVICE}", value);
			// itemDiscoveries.add(senderItemDiscovery);
			// }
			//
			// }
			//
			// Date dateDiscovery = new Date();
			// SimpleDateFormat formatDiscovery = new SimpleDateFormat(
			// Constants.DATE_FORMAT);
			//
			// String jsonData = this.sendItemsDiscovery(itemDiscoveries);
			// ZabbixSenderItem item = new ZabbixSenderItem(hostDiscovery,
			// "custom.service.discovery", jsonData);
			// ArrayList<ZabbixSenderItem> senderItem = new ArrayList<>();
			// senderItem.add(item);
			//
			// try {
			// response = this.sendItems(item);
			// LOG.info(formatDiscovery.format(dateDiscovery) + ": " +
			// response.toString());
			// } catch (IOException e) {
			// LOG.error("Error sending itens.", e);
			// }
		}
	}

	@Override
	protected void close() {
		try {
			sock.close();
		} catch (IOException e) {
			LOG.error("Error closing connection.", e);
		}
	}

}
