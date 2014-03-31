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
package org.jboss.windup.metadata.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

public class NamespaceMapContext implements NamespaceContext {
	private final Map<String, String> context = new HashMap<String, String>();

	public NamespaceMapContext() {

	}

	public NamespaceMapContext(Map<String, String> in) {
		if (in != null && in.size() > 0) {
			context.putAll(in);
		}
	}

	public String getNamespaceURI(String prefix) {
		return context.get(prefix);
	}

	public String getPrefix(String namespaceURI) {
		Iterator<String> prefixIterator = getPrefixes(namespaceURI);

		if (prefixIterator.hasNext()) {
			return getPrefixes(namespaceURI).next();
		}
		return null;
	}

	public Iterator<String> getPrefixes(String namespaceURI) {
		List<String> prefixes = new LinkedList<String>();

		for (String key : context.keySet()) {
			// slow but works.
			if (namespaceURI.equals(context.get(key))) {
				prefixes.add(key);
			}
		}

		return prefixes.iterator();
	}
}
