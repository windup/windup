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
package org.jboss.windup.decorator.xml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.decorator.gate.XPathGateDecorator;
import org.jboss.windup.metadata.decoration.Classification;
import org.jboss.windup.metadata.type.XmlMetadata;
import org.springframework.beans.factory.InitializingBean;


public class XPathClassifyingDecorator extends XPathGateDecorator implements InitializingBean {
	private static final Log LOG = LogFactory.getLog(XPathClassifyingDecorator.class);

	protected String matchDescription;

	public void setMatchDescription(String matchDescription) {
		this.matchDescription = matchDescription;
	}

	@Override
	protected void chainDecorators(XmlMetadata meta) {
		LOG.debug("chainDecorators: " + meta);
		// this is only called when the XPathGate is true.
		Classification result = new Classification();
		result.setDescription(matchDescription);
		result.setEffort(effort);
		result.setPattern(this.xpathExpression);
		meta.getDecorations().add(result);

		super.chainDecorators(meta);
	}
}
