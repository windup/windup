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
package org.jboss.windup.metadata.decoration.hint;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.util.XmlCDataAdapter;
import org.pegdown.PegDownProcessor;


@XmlRootElement(name="markdown-hint")
public class MarkdownHint extends Hint {
	private static final Log LOG = LogFactory.getLog(MarkdownHint.class);
	
	private String markdown;

	public void setMarkdown(String markdown) {
		this.markdown = markdown;
	}
	
	@XmlJavaTypeAdapter(XmlCDataAdapter.class)
	@XmlElement(name="markdown")
	public String getMarkdown() {
		return markdown;
	}
	
	@Override
	public String toString() {
		PegDownProcessor mdProcessor = new PegDownProcessor();
		return mdProcessor.markdownToHtml(markdown);
	}
}
