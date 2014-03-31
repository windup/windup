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

import org.jboss.windup.decorator.xml.XSLTDecorator;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;


public class XSLTBeanParser extends AbstractBeanDefinitionParser {

	/**
	 * <property name="xsltLocation" value="transformations/xslt/jboss-app-to-jboss5.xsl" />
	 * <property name="outputDescription" value="JBoss APP Descriptor (Windup-Generated)" />
	 * <property name="outputExtension" value="(Windup-Generated).xml" />
	 */
	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.rootBeanDefinition(XSLTDecorator.class);
		beanBuilder.addPropertyValue("xsltLocation", element.getAttribute("location"));
		beanBuilder.addPropertyValue("outputDescription", element.getAttribute("description"));
		beanBuilder.addPropertyValue("outputExtension", element.getAttribute("extension"));

		return beanBuilder.getBeanDefinition();
	}

}
