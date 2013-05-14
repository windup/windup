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
package org.jboss.windup.decorator.xml;

import org.jboss.windup.resource.type.XmlMeta;
import org.w3c.dom.Node;

public class XPathValueDecorator extends XPathSummaryDecorator {
	@Override
	protected void createLineNumberMeta(XmlMeta meta, Integer lineNumber, String descripiton, Node match) {
		super.createLineNumberMeta(meta, lineNumber, descripiton + ": " + match, match);
	}

	@Override
	protected void createSummaryMeta(XmlMeta meta, String description, String match) {
		super.createSummaryMeta(meta, description + ": " + match, match);
	}
}
