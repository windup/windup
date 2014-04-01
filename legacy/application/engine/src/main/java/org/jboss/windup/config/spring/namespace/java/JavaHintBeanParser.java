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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.jboss.windup.decorator.java.JavaPatternHintProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;


public class JavaHintBeanParser extends AbstractBeanDefinitionParser {

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.rootBeanDefinition(JavaPatternHintProcessor.class);
		beanBuilder.addPropertyValue("regexPattern", element.getAttribute("regex"));
		
		if(element.hasAttribute("hint")) {
			beanBuilder.addPropertyValue("hint", element.getAttribute("hint"));
		}
		else {
			String markdown = element.getTextContent();
			
			String lines[] = markdown.split("\\r?\\n");
			StringBuilder markdownRebuilder = new StringBuilder();
			
			for(String line : lines) {
				line = StringUtils.trim(line);
				if(line != null) {
					markdownRebuilder.append(line).append(SystemUtils.LINE_SEPARATOR);
				}
			}
			beanBuilder.addPropertyValue("hint", markdownRebuilder.toString());
		}
		
		if (element.hasAttribute("source-type")) {
			beanBuilder.addPropertyValue("sourceType", element.getAttribute("source-type"));
		}
		if (element.hasAttribute("effort")) {
			beanBuilder.addPropertyValue("effort", Integer.parseInt(element.getAttribute("effort")));
		}

		return beanBuilder.getBeanDefinition();
	}

}
