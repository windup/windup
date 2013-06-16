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
package org.jboss.windup.config.spring.namespace.xml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.config.spring.namespace.java.SpringNamespaceHandlerUtil;
import org.jboss.windup.decorator.xml.XPathSummaryDecorator;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;


public class XPathSummaryBeanParser extends AbstractBeanDefinitionParser {
	private static final Log LOG = LogFactory.getLog(XPathSummaryBeanParser.class);

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.rootBeanDefinition(XPathSummaryDecorator.class);

		beanBuilder.addPropertyValue("xpathExpression", element.getAttribute("xpath"));
		beanBuilder.addPropertyValue("matchDescription", element.getAttribute("description"));

		if (element.hasAttribute("inline")) {
			beanBuilder.addPropertyValue("inline", element.getAttribute("inline"));
		}

		if (element.hasAttribute("effort")) {
			LOG.debug("Effort: " + element.getAttribute("effort"));
			BeanDefinitionBuilder effortBean = BeanDefinitionBuilder.genericBeanDefinition("org.jboss.windup.metadata.decoration.effort.StoryPointEffort");
			effortBean.addPropertyValue("hours", element.getAttribute("effort"));
			beanBuilder.addPropertyValue("effort", effortBean.getBeanDefinition());
		}
		else {
			BeanDefinitionBuilder effortBean = BeanDefinitionBuilder.genericBeanDefinition("org.jboss.windup.metadata.decoration.effort.UnknownEffort");
			beanBuilder.addPropertyValue("effort", effortBean.getBeanDefinition());
		}

		SpringNamespaceHandlerUtil.parseNamespaceMap(beanBuilder, element);

		SpringNamespaceHandlerUtil.setNestedList(beanBuilder, element, "hints", parserContext);
		SpringNamespaceHandlerUtil.setNestedList(beanBuilder, element, "decorators", parserContext);

		return beanBuilder.getBeanDefinition();
	}

}
