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

import java.util.Collection;
import java.util.TreeSet;

public class ArchiveReport extends ResourceReport {
	
	private boolean vendorResult = true;
	
	private Collection<ArchiveReport> nestedArchiveReports = new TreeSet<ArchiveReport>();
	private Collection<ResourceReport> resourceReports = new TreeSet<ResourceReport>();
	private Collection<ResourceReport> additionalInformationReports = new TreeSet<ResourceReport>();
	
	public void setVendorResult(boolean vendorResult) {
		this.vendorResult = vendorResult;
	}
	
	public boolean isVendorResult() {
		return vendorResult;
	}
	
	public Collection<ArchiveReport> getNestedArchiveReports() {
		return nestedArchiveReports;
	}
	
	public void setNestedArchiveReports(
			Collection<ArchiveReport> nestedArchiveReports) {
		this.nestedArchiveReports = nestedArchiveReports;
	}
	
	public Collection<ResourceReport> getResourceReports() {
		return resourceReports;
	}
	
	public void setResourceReports(Collection<ResourceReport> resourceReports) {
		this.resourceReports = resourceReports;
	}
	
	public void setAdditionalInformationReports(
			Collection<ResourceReport> additionalInformationReports) {
		this.additionalInformationReports = additionalInformationReports;
	}
	
	public Collection<ResourceReport> getAdditionalInformationReports() {
		return additionalInformationReports;
	}
}
