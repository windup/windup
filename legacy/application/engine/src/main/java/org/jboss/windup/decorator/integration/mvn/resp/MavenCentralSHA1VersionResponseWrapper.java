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

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties({ "responseHeader" })
public class MavenCentralSHA1VersionResponseWrapper {

	private MavenCentralSHA1VersionResponse response;

	public MavenCentralSHA1VersionResponse getResponse() {
		return response;
	}

	public void setResponse(MavenCentralSHA1VersionResponse response) {
		this.response = response;
	}

	@Override
	public String toString() {
		return "MavenCentralSHA1VersionResponseWrapper [response=" + response + "]";
	}

}
