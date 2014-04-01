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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class BlacklistPackageResolver {

	private Set<Pattern> generatedBlacklists;
	private Set<Pattern> classBlacklists;
	
	public void setGeneratedBlacklists(Set<Pattern> generatedBlacklists) {
		this.generatedBlacklists = generatedBlacklists;
	}
	public void setClassBlacklists(Set<Pattern> classBlacklists) {
		this.classBlacklists = classBlacklists;
	}

	protected boolean classContainedInList(Set<String> classImports, Set<Pattern> targetList) {
		for (String classImport : classImports) {
			for (Pattern listElement : targetList) {
				if (listElement.matcher(classImport).find()) {
					return true;
				}
			}
		}

		return false;
	}
	
	protected Set<String> extractMatchingEntries(Set<String> candidates, Set<Pattern> targetList) {
		Set<String> lists = new HashSet<String>();
		for (String clzImport : candidates) {
			for (Pattern listElement : targetList) {
				if (listElement.matcher(clzImport).find()) {
					lists.add(clzImport);
				}
			}
		}

		return lists;
	}
	
	public Set<String> extractBlacklist(Set<String> classImports) {
		return extractMatchingEntries(classImports, this.classBlacklists);
	}
	
	
	public boolean containsBlacklist(Set<String> classImports) {
		return classContainedInList(classImports, this.classBlacklists);
	}

	public boolean containsGenerated(Set<String> classImports) {
		return classContainedInList(classImports, this.generatedBlacklists);
	}

}
