package com.ironmountain.rmaas.activiti.zorroa.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZorroaDocument {

	private Zorroa zorroa;
	private Proxies proxies;
	private Source source;
	private Media media;

	public Zorroa getZorroa() {
		return zorroa;
	}

	public void setZorroa(Zorroa zorroa) {
		this.zorroa = zorroa;
	}

	public Proxies getProxies() {
		return proxies;
	}

	public void setProxies(Proxies proxies) {
		this.proxies = proxies;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

	public Media getMedia() {
		return media;
	}

	public void setMedia(Media media) {
		this.media = media;
	}

}
