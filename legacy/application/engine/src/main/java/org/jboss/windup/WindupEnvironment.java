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
package org.jboss.windup;

/***
 * Runtime configuration for the Windup environment
 * @author bradsdavis
 *
 */
public class WindupEnvironment {

	private String excludeSignature;
	private String packageSignature;
	private String targetPlatform;
	private String fetchRemote;

	private String logLevel;
	private boolean captureLog;
	private boolean source;
	
	public void setSource(boolean source) {
		this.source = source;
	}
	
	public boolean isSource() {
		return source;
	}
	
	public void setExcludeSignature(String excludeSignature) {
		this.excludeSignature = excludeSignature;
	}
	
	public String getExcludeSignature() {
		return excludeSignature;
	}
	
	public String getPackageSignature() {
		return packageSignature;
	}

	public void setPackageSignature(String packageSignature) {
		this.packageSignature = packageSignature;
	}

	public String getTargetPlatform() {
		return targetPlatform;
	}

	public void setTargetPlatform(String targetPlatform) {
		this.targetPlatform = targetPlatform;
	}

	public String getFetchRemote() {
		return fetchRemote;
	}

	public void setFetchRemote(String fetchRemote) {
		this.fetchRemote = fetchRemote;
	}

	public String getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}

	public boolean isCaptureLog() {
		return captureLog;
	}

	public void setCaptureLog(boolean captureLog) {
		this.captureLog = captureLog;
	}

}
