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
import org.jboss.windup.decorator.xml.DTDPatternClassifyingDecorator;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;


public class DTDClassifyingBeanParser extends AbstractBeanDefinitionParser {
	private static final Log LOG = LogFactory.getLog(DTDClassifyingBeanParser.class);

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.rootBeanDefinition(DTDPatternClassifyingDecorator.class);

		beanBuilder.addPropertyValue("matchDescription", element.getAttribute("description"));

		if (element.hasAttribute("name-regex")) {
			beanBuilder.addPropertyValue("namePattern", element.getAttribute("name-regex"));
		}
		if (element.hasAttribute("public-id-regex")) {
			beanBuilder.addPropertyValue("publicIdPattern", element.getAttribute("public-id-regex"));
		}
		if (element.hasAttribute("system-id-regex")) {
			beanBuilder.addPropertyValue("systemIdPattern", element.getAttribute("system-id-regex"));
		}
		if (element.hasAttribute("base-uri-regex")) {
			beanBuilder.addPropertyValue("baseURIPattern", element.getAttribute("base-uri-regex"));
		}

		SpringNamespaceHandlerUtil.setNestedList(beanBuilder, element, "hints", parserContext);
		SpringNamespaceHandlerUtil.setNestedList(beanBuilder, element, "decorators", parserContext);

		return beanBuilder.getBeanDefinition();
	}
}
