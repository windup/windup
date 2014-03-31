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

import org.jboss.windup.decorator.java.JavaWhitelistPatternProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;


public class JavaWhitelistBeanParser extends AbstractBeanDefinitionParser {

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.rootBeanDefinition(JavaWhitelistPatternProcessor.class);
		beanBuilder.addPropertyValue("regexPattern", element.getAttribute("regex"));
		if (element.hasAttribute("source-type")) {
			beanBuilder.addPropertyValue("sourceType", element.getAttribute("source-type"));
		}

		return beanBuilder.getBeanDefinition();
	}

}
