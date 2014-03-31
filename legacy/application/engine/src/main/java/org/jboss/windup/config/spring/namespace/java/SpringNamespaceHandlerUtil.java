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
package org.jboss.windup.config.spring.namespace.java;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.util.XmlElementUtil;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;


public class SpringNamespaceHandlerUtil {
	private static final Log LOG = LogFactory.getLog(SpringNamespaceHandlerUtil.class);

	public static void parseNamespaceMap(BeanDefinitionBuilder builder, Element element) {

		List<Element> elements = XmlElementUtil.getChildrenByTagName(element, "namespace");
		if (elements == null) {
			return; // do nothing.
		}

		ManagedMap<String, String> namespaceMap = new ManagedMap<String, String>(elements.size());
		for (Element bean : elements) {
			String prefix = bean.getAttribute("prefix");
			String namespace = bean.getAttribute("uri");

			namespaceMap.put(prefix, namespace);
		}
		LOG.debug("Adding namespaces: " + namespaceMap.size());
		builder.addPropertyValue("namespaces", namespaceMap);
	}

	public static BeanDefinition resolveBeanDefinition(BeanDefinition beanDef, Element element, ParserContext context) {
		BeanDefinitionParserDelegate delegate = context.getDelegate();
		String namespace = element.getNamespaceURI();

		// check to see whether it is the default Spring bean decorator...
		if (StringUtils.equals(namespace, BeanDefinitionParserDelegate.BEANS_NAMESPACE_URI)) {
			BeanDefinitionHolder holder = delegate.parseBeanDefinitionElement(element, beanDef);
			return holder.getBeanDefinition();
		}

		// otherwise, see if it is supported based on our namespace resolver...
		NamespaceHandler namespaceHandler = delegate.getReaderContext().getNamespaceHandlerResolver().resolve(namespace);
		if (namespaceHandler == null) {
			throw new FatalBeanException("Unable to find parser for bean with namespace: " + namespace);
		}

		return namespaceHandler.parse(element, new ParserContext(delegate.getReaderContext(), delegate, beanDef));
	}

	public static void setNestedList(BeanDefinitionBuilder beanBuilder, Element bean, String nestedTagName, ParserContext context) {
		BeanDefinition beanDef = beanBuilder.getRawBeanDefinition();

		Element test = XmlElementUtil.getChildByTagName(bean, nestedTagName);
		if (test != null) {
			if (test.hasAttribute("ref")) {
				beanBuilder.addPropertyReference(nestedTagName, test.getAttribute("ref"));
				return;
			}
		}

		Element nestElement = XmlElementUtil.getChildByTagName(bean, nestedTagName);
		if (nestElement != null) {
			ManagedList<BeanDefinition> nested = SpringNamespaceHandlerUtil.parseManagedList(beanDef,
					XmlElementUtil.getChildElements(nestElement), context);
			beanBuilder.addPropertyValue(nestedTagName, nested);
		}
	}

	public static void setNestedMap(BeanDefinitionBuilder beanBuilder, Element bean, String nestedTagName, ParserContext context) {
		BeanDefinition beanDef = beanBuilder.getRawBeanDefinition();

		Element nestElement = XmlElementUtil.getChildByTagName(bean, nestedTagName);
		if (nestElement != null) {
			// ManagedMap<String, String> nested = SpringNamespaceHandlerUtil.parseManagedList(beanDef,
			// SpringNamespaceHandlerUtil.getChildElements(nestElement), context);
			// beanBuilder.addPropertyValue(nestedTagName, nested);
		}
	}

	public static ManagedList<BeanDefinition> parseManagedList(BeanDefinition beanDef, List<Element> elements, ParserContext context) {
		if (elements == null) {
			return null;
		}
		ManagedList<BeanDefinition> beanDefs = new ManagedList<BeanDefinition>(elements.size());
		for (Element bean : elements) {
			BeanDefinition def = resolveBeanDefinition(beanDef, bean, context);
			beanDefs.add(def);
		}

		return beanDefs;
	}
}
