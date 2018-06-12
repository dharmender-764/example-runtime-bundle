package com.ironmountain.rmaas.activiti.zorroa.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Links {

	@JsonProperty(value = "import")
	private List<String> importLinks;

	public List<String> getImportLinks() {
		return importLinks;
	}

	public void setImportLinks(List<String> importLinks) {
		this.importLinks = importLinks;
	}

}
