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
package org.jboss.windup.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CustomerPackageResolver {

	private static final Log LOG = LogFactory.getLog(CustomerPackageResolver.class);

	private Set<String> customerPackages = new HashSet<String>();
	private Set<String> excludePackages = new HashSet<String>();

	public Set<String> getExcludePackages() {
		return excludePackages;
	}
	
	public Set<String> getCustomerPackages() {
		return customerPackages;
	}

	public CustomerPackageResolver(String packageSignatures, String excludeSignatures) {
		populateSignature(packageSignatures, customerPackages);
		populateSignature(excludeSignatures, excludePackages);
	}

	public void populateSignature(String raw, Set<String> signatures) {
		
		if (StringUtils.isNotBlank(raw)) {
			String[] splitSignatures = StringUtils.split(raw, ":");
			if (splitSignatures != null && splitSignatures.length > 0) {
				for (String pkg : splitSignatures) {
					LOG.info("Found Package: " + pkg);
				}
				signatures.addAll(Arrays.asList(splitSignatures));
			}
		}
	}
	
	public boolean isCustomerPkg(String className) {
		for (String excludePackage : excludePackages) {
			if (StringUtils.startsWith(className, excludePackage)) {
				return false;
			}
		}
		
		
		for (String customerPackage : customerPackages) {
			if (StringUtils.startsWith(className, customerPackage)) {
				return true;
			}
		}
		return false;
	}
}
