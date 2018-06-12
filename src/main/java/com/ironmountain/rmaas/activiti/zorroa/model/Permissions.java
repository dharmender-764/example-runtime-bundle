package com.ironmountain.rmaas.activiti.zorroa.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Permissions {

	private List<String> read;
	private List<String> export;
	private List<String> write;

	public List<String> getRead() {
		return read;
	}

	public void setRead(List<String> read) {
		this.read = read;
	}

	public List<String> getExport() {
		return export;
	}

	public void setExport(List<String> export) {
		this.export = export;
	}

	public List<String> getWrite() {
		return write;
	}

	public void setWrite(List<String> write) {
		this.write = write;
	}

}
