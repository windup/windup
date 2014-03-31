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

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlElementUtil {
	private XmlElementUtil() {
		// seal
	}

	public static Element getChildByTagName(Element element, String tagName) {
		List<Element> elements = getChildrenByTagName(element, tagName);
		if (elements == null || elements.size() == 0) {
			return null;
		}

		return elements.get(0);
	}

	public static List<Element> getChildElements(Element element) {
		List<Element> elements = new LinkedList<Element>();

		NodeList children = element.getChildNodes();
		if (children == null) {
			return elements;
		}

		for (int i = 0, j = children.getLength(); i < j; i++) {
			Node child = children.item(i);

			if (child.getNodeType() == Node.ELEMENT_NODE) {
				elements.add((Element) child);
			}
		}

		return elements;
	}

	public static List<Element> getChildrenByTagName(Element element, String tagName) {
		List<Element> elements = new LinkedList<Element>();

		NodeList children = element.getChildNodes();
		if (children == null) {
			return elements;
		}

		for (int i = 0, j = children.getLength(); i < j; i++) {
			Node child = children.item(i);

			if (child.getNodeType() == Node.ELEMENT_NODE) {
				if (StringUtils.equals(child.getLocalName(), tagName))
				{
					elements.add((Element) child);
				}
			}
		}

		return elements;
	}
}
