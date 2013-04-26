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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.decorator.ChainingDecorator;
import org.jboss.windup.decorator.xml.util.LocationContentHandler;
import org.jboss.windup.decorator.xml.util.LocationContentHandler.Doctype;
import org.jboss.windup.resource.decoration.Classification;
import org.jboss.windup.resource.type.XmlMeta;


public class DTDPatternClassifyingDecorator extends ChainingDecorator<XmlMeta> {
	private String matchDescription;

	private Pattern namePattern;
	private Pattern publicIdPattern;
	private Pattern systemIdPattern;
	private Pattern baseURIPattern;

	public void setMatchDescription(String matchDescription) {
		this.matchDescription = matchDescription;
	}

	public void setNamePattern(Pattern namePattern) {
		this.namePattern = namePattern;
	}

	public void setPublicIdPattern(Pattern publicIdPattern) {
		this.publicIdPattern = publicIdPattern;
	}

	public void setSystemIdPattern(Pattern systemIdPattern) {
		this.systemIdPattern = systemIdPattern;
	}

	public void setBaseURIPattern(Pattern baseURIPattern) {
		this.baseURIPattern = baseURIPattern;
	}

	@Override
	public void processMeta(XmlMeta file) {
		Doctype docType = (Doctype) file.getParsedDocument().getUserData(LocationContentHandler.DOCTYPE_KEY_NAME);

		if (docType != null && matchesAny(docType)) {
			Classification cr = new Classification();
			cr.setDescription(matchDescription);
			cr.setEffort(effort);
			file.getDecorations().add(cr);

			// if it is of a certain type, recurse to the child processors
			chainDecorators(file);
		}
	}

	protected boolean matchesAny(Doctype docType) {
		return processPattern(namePattern, docType.getName()) ||
				processPattern(publicIdPattern, docType.getPublicId()) ||
				processPattern(systemIdPattern, docType.getSystemId()) ||
				processPattern(baseURIPattern, docType.getBaseURI());
	}

	protected boolean processPattern(Pattern pattern, String val) {
		if (pattern != null && StringUtils.isNotBlank(val)) {
			Matcher matcher = pattern.matcher(val);
			return matcher.find();
		}
		return false;
	}
}
