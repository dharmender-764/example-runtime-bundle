package com.ironmountain.rmaas.activiti.zorroa.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Document {

	private String id;
	private String type;
	private Double score;
	private ZorroaDocument document;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public ZorroaDocument getDocument() {
		return document;
	}

	public void setDocument(ZorroaDocument document) {
		this.document = document;
	}

}
