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
import org.jboss.windup.metadata.decoration.Classification;
import org.jboss.windup.metadata.type.XmlMetadata;
import org.jboss.windup.metadata.util.LocationAwareContentHandler;
import org.jboss.windup.metadata.util.LocationAwareContentHandler.Doctype;


public class DTDPatternClassifyingDecorator extends ChainingDecorator<XmlMetadata> {
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
	public void processMeta(XmlMetadata file) {
		Doctype docType = (Doctype) file.getParsedDocument().getUserData(LocationAwareContentHandler.DOCTYPE_KEY_NAME);

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
