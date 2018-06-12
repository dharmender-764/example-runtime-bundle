package com.ironmountain.rmaas.activiti.zorroa.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Zorroa {

	private Permissions permissions;
	private Links links;
	private Date timeCreated;
	private Date timeModified;

	public Permissions getPermissions() {
		return permissions;
	}

	public void setPermissions(Permissions permissions) {
		this.permissions = permissions;
	}

	public Links getLinks() {
		return links;
	}

	public void setLinks(Links links) {
		this.links = links;
	}

	public Date getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(Date timeCreated) {
		this.timeCreated = timeCreated;
	}

	public Date getTimeModified() {
		return timeModified;
	}

	public void setTimeModified(Date timeModified) {
		this.timeModified = timeModified;
	}

}
