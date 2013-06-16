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

import java.util.List;
import java.util.Map;

public class ClasspathReport extends ResourceReport {

	private final Map<String, List<String>> missingToAffected;
	
	public ClasspathReport(Map<String, List<String>> missingToAffected) {
		this.missingToAffected = missingToAffected;
	}
	
	public Map<String, List<String>> getMissingToAffected() {
		return missingToAffected;
	}
}
