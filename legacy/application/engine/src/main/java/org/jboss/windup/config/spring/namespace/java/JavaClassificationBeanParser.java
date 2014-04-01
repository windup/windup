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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.decorator.java.JavaClassifyingDecorator;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;


public class JavaClassificationBeanParser extends AbstractBeanDefinitionParser {
	private static final Log LOG = LogFactory.getLog(JavaClassificationBeanParser.class);

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.rootBeanDefinition(JavaClassifyingDecorator.class);

		beanBuilder.addPropertyValue("regexPattern", element.getAttribute("regex"));
		beanBuilder.addPropertyValue("matchDescription", element.getAttribute("description"));
		if (element.hasAttribute("source-type")) {
			beanBuilder.addPropertyValue("sourceType", element.getAttribute("source-type"));
		}
		if (element.hasAttribute("effort")) {
			beanBuilder.addPropertyValue("effort", Integer.parseInt(element.getAttribute("effort")));
		}

		SpringNamespaceHandlerUtil.setNestedList(beanBuilder, element, "hints", parserContext);
		SpringNamespaceHandlerUtil.setNestedList(beanBuilder, element, "decorators", parserContext);

		return beanBuilder.getBeanDefinition();
	}
}
