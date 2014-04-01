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
package org.jboss.windup.config.spring.namespace.interrogator;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.config.spring.namespace.java.SpringNamespaceHandlerUtil;
import org.jboss.windup.util.XmlElementUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;


public class InterrogatorExtensionBeanParser extends AbstractBeanDefinitionParser {
	private static final Log LOG = LogFactory.getLog(InterrogatorExtensionBeanParser.class);

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.rootBeanDefinition(org.springframework.beans.factory.config.MethodInvokingFactoryBean.class);

		InterrogatorTypeEnum interrogatorTarget = InterrogatorTypeEnum.valueOf(element.getAttribute("type"));
		String targetObject = interrogatorTarget.getBeanName();

		LOG.debug("Resolved target extension: " + targetObject);

		beanBuilder.addPropertyReference("targetObject", targetObject);
		beanBuilder.addPropertyValue("targetMethod", "addDecorators");

		// parse list..
		parseDecoratorExtensions(beanBuilder, element, parserContext);
		return beanBuilder.getBeanDefinition();
	}

	private void parseDecoratorExtensions(BeanDefinitionBuilder builder, Element element, ParserContext parserContext) {
		BeanDefinition beanDef = builder.getRawBeanDefinition();

		List<Element> elements = XmlElementUtil.getChildElements(element);
		if (elements == null) {
			return; // do nothing.
		}

		ManagedList<BeanDefinition> decorations = SpringNamespaceHandlerUtil.parseManagedList(beanDef, elements, parserContext);

		LOG.debug("Adding decorators: " + decorations.size());
		builder.addPropertyValue("arguments", decorations);
	}

	private enum InterrogatorTypeEnum {
		XML("xmlPipeline"),
		JAVA("javaPipeline"),
		JSP("jspPipeline"),
		ARCHIVE("archivePipeline");

		private final String beanName;

		InterrogatorTypeEnum(String beanName) {
			this.beanName = beanName;
		}

		public String getBeanName() {
			return beanName;
		}
	}
}
