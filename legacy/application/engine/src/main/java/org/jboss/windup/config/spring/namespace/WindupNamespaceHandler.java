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
package org.jboss.windup.config.spring.namespace;

import org.jboss.windup.config.spring.namespace.gate.JavaPatternGateBeanParser;
import org.jboss.windup.config.spring.namespace.gate.RegexFileGateBeanParser;
import org.jboss.windup.config.spring.namespace.gate.RegexPatternGateBeanParser;
import org.jboss.windup.config.spring.namespace.interrogator.InterrogatorExtensionBeanParser;
import org.jboss.windup.config.spring.namespace.java.JavaAddHintsBeanParser;
import org.jboss.windup.config.spring.namespace.java.JavaClassificationBeanParser;
import org.jboss.windup.config.spring.namespace.java.JavaHintBeanParser;
import org.jboss.windup.config.spring.namespace.java.JavaWhitelistBeanParser;
import org.jboss.windup.config.spring.namespace.simple.ClassificationBeanParser;
import org.jboss.windup.config.spring.namespace.simple.GlobalBeanParser;
import org.jboss.windup.config.spring.namespace.simple.LinkBeanParser;
import org.jboss.windup.config.spring.namespace.simple.RegexHintBeanParser;
import org.jboss.windup.config.spring.namespace.simple.SummaryBeanParser;
import org.jboss.windup.config.spring.namespace.xml.DTDClassifyingBeanParser;
import org.jboss.windup.config.spring.namespace.xml.XPathClassifyingBeanParser;
import org.jboss.windup.config.spring.namespace.xml.XPathSummaryBeanParser;
import org.jboss.windup.config.spring.namespace.xml.XPathValueBeanParser;
import org.jboss.windup.config.spring.namespace.xml.XSLTBeanParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;


public class WindupNamespaceHandler extends NamespaceHandlerSupport {

	public void init() {
		registerBeanDefinitionParser("java-classification", new JavaClassificationBeanParser());
		registerBeanDefinitionParser("java-whitelist", new JavaWhitelistBeanParser());
		
		registerBeanDefinitionParser("java-hint", new JavaHintBeanParser());
		registerBeanDefinitionParser("java-hints", new JavaAddHintsBeanParser());
		registerBeanDefinitionParser("java-gate", new JavaPatternGateBeanParser());
		registerBeanDefinitionParser("xpath-gate", new XPathClassifyingBeanParser());
		registerBeanDefinitionParser("xpath-classification", new XPathClassifyingBeanParser());
		registerBeanDefinitionParser("xpath-value", new XPathValueBeanParser());
		registerBeanDefinitionParser("xpath-summary", new XPathSummaryBeanParser());
		registerBeanDefinitionParser("dtd-classification", new DTDClassifyingBeanParser());
		registerBeanDefinitionParser("xslt-transform", new XSLTBeanParser());

		registerBeanDefinitionParser("hint", new JavaHintBeanParser());
		registerBeanDefinitionParser("classification", new ClassificationBeanParser());
		registerBeanDefinitionParser("global", new GlobalBeanParser());
		registerBeanDefinitionParser("summary", new SummaryBeanParser());
		registerBeanDefinitionParser("link", new LinkBeanParser());
		registerBeanDefinitionParser("regex-hint", new RegexHintBeanParser());
		registerBeanDefinitionParser("regex-gate", new RegexPatternGateBeanParser());
		registerBeanDefinitionParser("file-gate", new RegexFileGateBeanParser());

		registerBeanDefinitionParser("pipeline", new InterrogatorExtensionBeanParser());
	}

}
