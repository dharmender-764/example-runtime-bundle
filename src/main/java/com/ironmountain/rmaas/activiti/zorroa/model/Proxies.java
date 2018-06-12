package com.ironmountain.rmaas.activiti.zorroa.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Proxies {

	private List<Proxy> proxies;

	private List<String> tinyProxy;

	public List<Proxy> getProxies() {
		return proxies;
	}

	public void setProxies(List<Proxy> proxies) {
		this.proxies = proxies;
	}

	public List<String> getTinyProxy() {
		return tinyProxy;
	}

	public void setTinyProxy(List<String> tinyProxy) {
		this.tinyProxy = tinyProxy;
	}

}
