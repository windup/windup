/*
 * Copyright (c) 2013 Red Hat, Inc. and/or its affiliates.
 *  
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  Contributors:
 *      Brad Davis - bradsdavis@gmail.com - Initial API and implementation
*/
package org.jboss.windup.decorator.integration.mvn.resp;

import java.util.Arrays;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties({ "tags" })
public class MavenCentralSHA1VersionResponseItem {
	@JsonProperty("id")
	private String id;

	@JsonProperty("g")
	private String groupId;

	@JsonProperty("a")
	private String artifactId;

	@JsonProperty("v")
	private String version;

	@JsonProperty("p")
	private String type;

	@JsonProperty("timestamp")
	private String timestamp;

	@JsonProperty("ec")
	private String[] sources;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String[] getSources() {
		return sources;
	}

	public void setSources(String[] sources) {
		this.sources = sources;
	}

	@Override
	public String toString() {
		return "MavenCentralSHA1VersionResponseItem [id=" + id + ", groupId=" + groupId + ", artifactId=" + artifactId + ", version=" + version + ", type=" + type + ", timestamp=" + timestamp + ", sources=" + Arrays.toString(sources) + "]";
	}

}
