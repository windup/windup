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
package org.jboss.windup.graph.clz;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ClassContext {
	private Map<String, Set<GraphableClz>> classGraph = new HashMap<String, Set<GraphableClz>>();
	
	public Set<GraphableClz> getClass(String className) {
		if(classGraph.containsKey(className)) {
			return classGraph.get(className);
		}
		return null;
	}
	
	public boolean containsClass(String className) {
		return classGraph.containsKey(className);
	}
	
	public void addClass(GraphableClz clazz) {
		if(!classGraph.containsKey(clazz.getClassName())) {
			classGraph.put(clazz.getClassName(), new TreeSet<GraphableClz>());
		}
		classGraph.get(clazz.getClassName()).add(clazz);
	}
	
	public Set<GraphableClz> getClasses() {
		Set<GraphableClz> all = new TreeSet<GraphableClz>();
		
		for(Set<GraphableClz> classes : classGraph.values()) {
			all.addAll(classes);
		}
		
		return all;
	}
	
	public Set<String> getClassNames() {
		return classGraph.keySet();
	}
}
