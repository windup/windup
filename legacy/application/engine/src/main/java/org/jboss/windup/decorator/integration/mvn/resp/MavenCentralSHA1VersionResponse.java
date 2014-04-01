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

public class MavenCentralSHA1VersionResponse {

	private int numFound;
	private int start;
	private MavenCentralSHA1VersionResponseItem[] docs;

	public int getNumFound() {
		return numFound;
	}

	public void setNumFound(int numFound) {
		this.numFound = numFound;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public MavenCentralSHA1VersionResponseItem[] getDocs() {
		return docs;
	}

	public void setDocs(MavenCentralSHA1VersionResponseItem[] docs) {
		this.docs = docs;
	}

	@Override
	public String toString() {
		return "MavenCentralSHA1VersionResponse [numFound=" + numFound + ", start=" + start + ", docs=" + Arrays.toString(docs) + "]";
	}

}
