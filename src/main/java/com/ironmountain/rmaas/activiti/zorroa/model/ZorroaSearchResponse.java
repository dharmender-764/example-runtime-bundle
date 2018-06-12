package com.ironmountain.rmaas.activiti.zorroa.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ZorroaSearchResponse {

	private List<Object> queryTerms;
	private Page page;

	@JsonProperty(value = "list")
	private List<Document> documents;

	public List<Object> getQueryTerms() {
		return queryTerms;
	}

	public void setQueryTerms(List<Object> queryTerms) {
		this.queryTerms = queryTerms;
	}

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

}
