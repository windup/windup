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
package org.jboss.windup.reporting.html;

import org.jboss.windup.metadata.decoration.effort.Effort;
import org.jboss.windup.reporting.data.ResourceData;

public class ResourceReport extends ResourceData implements Comparable<ResourceReport> {
	private String title;
	private String summary;
	private Effort effort;
	
	private boolean[] sourceModification;
	private String relativePathFromRootToReport;

	public boolean[] getSourceModification() {
		return sourceModification;
	}
	
	public void setSourceModification(boolean[] sourceModification) {
		this.sourceModification = sourceModification;
	}
	
	public Effort getEffort() {
		return effort;
	}
	
	public void setEffort(Effort effort) {
		this.effort = effort;
	}
	
	public String getRelativePathFromRootToReport() {
		return relativePathFromRootToReport;
	}
	public void setRelativePathFromRootToReport(String relativePathFromRootToReport) {
		this.relativePathFromRootToReport = relativePathFromRootToReport;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	@Override
	public int compareTo(ResourceReport o) {
		if(o == null) {
			return -1;
		}
		if(o.getRelativePathToRoot() == null) {
			return -1;
		}
		if(this.getRelativePathFromRoot() == null) {
			return 1;
		}
		return this.getRelativePathFromRoot().compareTo(o.getRelativePathFromRoot());
	}
}
