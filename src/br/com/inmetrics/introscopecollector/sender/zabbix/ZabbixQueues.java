package br.com.inmetrics.introscopecollector.sender.zabbix;

import java.sql.ResultSet;
import java.util.concurrent.LinkedBlockingQueue;

import br.com.inmetrics.introscopecollector.util.queue.Queues;

public class ZabbixQueues extends Queues {
	
	private LinkedBlockingQueue<ResultSet> discoveryListIn;
	private LinkedBlockingQueue<String> discoveryListOut;
	
	public ZabbixQueues() {
		super();
		this.discoveryListIn = new LinkedBlockingQueue<ResultSet>();
		this.discoveryListOut = new LinkedBlockingQueue<String>();
	}
	
	public LinkedBlockingQueue<ResultSet> getDiscoveryList() {
		return discoveryListIn;
	}

	public void setDiscoveryList(LinkedBlockingQueue<ResultSet> discoveryList) {
		this.discoveryListIn = discoveryList;
	}

	public LinkedBlockingQueue<ResultSet> getDiscoveryListIn() {
		return discoveryListIn;
	}

	public void setDiscoveryListIn(LinkedBlockingQueue<ResultSet> discoveryListIn) {
		this.discoveryListIn = discoveryListIn;
	}

	public LinkedBlockingQueue<String> getDiscoveryListOut() {
		return discoveryListOut;
	}

	public void setDiscoveryListOut(LinkedBlockingQueue<String> discoveryListOut) {
		this.discoveryListOut = discoveryListOut;
	}
}
