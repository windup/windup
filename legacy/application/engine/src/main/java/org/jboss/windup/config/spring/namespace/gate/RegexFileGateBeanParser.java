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
package org.jboss.windup.config.spring.namespace.gate;

import org.jboss.windup.config.spring.namespace.java.SpringNamespaceHandlerUtil;
import org.jboss.windup.decorator.gate.RegexFileGateDecorator;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;


public class RegexFileGateBeanParser extends AbstractBeanDefinitionParser {

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.rootBeanDefinition(RegexFileGateDecorator.class);
		beanBuilder.addPropertyValue("regexPattern", element.getAttribute("regex"));

		if (element.hasAttribute("full-path")) {
			beanBuilder.addPropertyValue("fullPath", element.getAttribute("full-path"));
		}

		SpringNamespaceHandlerUtil.setNestedList(beanBuilder, element, "decorators", parserContext);

		return beanBuilder.getBeanDefinition();
	}

}
