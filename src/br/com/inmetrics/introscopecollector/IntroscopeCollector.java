package br.com.inmetrics.introscopecollector;

import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils;
import br.com.inmetrics.introscopecollector.util.properties.ResourceUtils.Constants;


public class IntroscopeCollector {

	private ResourceUtils resourceUtils;

	public ResourceUtils getResourceUtils() {
		return resourceUtils;
	}

	public boolean initializeAgent(String properfiesFile) {
		try {
			resourceUtils = new ResourceUtils(properfiesFile);
			System.out.println("Agent Names: " + resourceUtils.getProperty(Constants.INTROSCOPE_AGENT_NAMES));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

}
