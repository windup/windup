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
package org.jboss.windup.decorator.archive;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.decorator.MetaDecorator;
import org.jboss.windup.interrogator.util.KnownArchiveProfiler;
import org.jboss.windup.metadata.decoration.AbstractDecoration;
import org.jboss.windup.metadata.decoration.archetype.version.PomVersion;
import org.jboss.windup.metadata.type.XmlMetadata;
import org.jboss.windup.metadata.type.archive.ZipMetadata;
import org.springframework.beans.factory.InitializingBean;
import org.w3c.dom.Document;


public class PomVersionDecorator implements MetaDecorator<XmlMetadata>, InitializingBean {
	private static final Log LOG = LogFactory.getLog(PomVersionDecorator.class);

	private static final XPathFactory factory = XPathFactory.newInstance();

	private static final String GROUP_ID = "/*[local-name()='project']/*[local-name()='groupId']";
	private static final String ARTIFACT_ID = "/*[local-name()='project']/*[local-name()='artifactId']";
	private static final String VERSION = "/*[local-name()='project']/*[local-name()='version']";
	private static final String NAME = "/*[local-name()='project']/*[local-name()='name']";

	private static final String PARENT_GROUP_ID = "/*[local-name()='project']/*[local-name()='parent']/*[local-name()='groupId']";
	private static final String PARENT_VERSION = "/*[local-name()='project']/*[local-name()='parent']/*[local-name()='version']";

	protected XPathExpression groupIdXPath;
	protected XPathExpression artifactIdXPath;
	protected XPathExpression versionXPath;
	protected XPathExpression nameXPath;

	protected XPathExpression parentGroupIdXPath;
	protected XPathExpression parentVersionXPath;

	protected KnownArchiveProfiler knownArchiveProfiler;

	public void setKnownArchiveProfiler(KnownArchiveProfiler knownArchiveProfiler) {
		this.knownArchiveProfiler = knownArchiveProfiler;
	}

	@Override
	public void processMeta(XmlMetadata file) {
		if (!isActive(file)) {
			return;
		}

		Document doc = file.getParsedDocument();
		try {
			String groupId = extractStringValue(groupIdXPath, doc);

			if (StringUtils.isBlank(groupId) || StringUtils.startsWith(groupId, "${")) {
				LOG.debug("GroupId not found for file: " + file.getFilePointer().getAbsolutePath() + "; trying parent groupId...");
				// then check for parent...
				groupId = extractStringValue(parentGroupIdXPath, doc);
				if (StringUtils.isBlank(groupId)) {
					LOG.debug("Parent groupId not found for file: " + file.getFilePointer().getAbsolutePath() + "; skipping.");
					return;
				}
			}

			String version = extractStringValue(versionXPath, doc);
			if (StringUtils.isBlank(version) || StringUtils.startsWith(version, "${")) {
				LOG.debug("Version not found for file: " + file.getFilePointer().getAbsolutePath() + "; trying parent version...");
				// then check the parent...
				version = extractStringValue(parentVersionXPath, doc);
				if (StringUtils.isBlank(version)) {
					LOG.debug("Parent version not found for file: " + file.getFilePointer().getAbsolutePath() + "; skipping.");
					return;
				}
			}

			String artifactId = extractStringValue(artifactIdXPath, doc);
			if (StringUtils.isBlank(artifactId)) {
				LOG.debug("ArtifactId not found for file: " + file.getFilePointer().getAbsolutePath());
				return;
			}

			String name = extractStringValue(nameXPath, doc);
			createVersionResult(file, groupId, artifactId, version, name);
		}
		catch (XPathExpressionException e) {
			LOG.error("Exception running xpath expression.", e);
		}

	}

	protected String extractStringValue(XPathExpression expression, Document doc) throws XPathExpressionException {
		return (String) expression.evaluate(doc, XPathConstants.STRING);
	}

	protected boolean isActive(XmlMetadata file) {
		for (AbstractDecoration dr : file.getArchiveMeta().getDecorations()) {
			if (dr instanceof PomVersion) {
				LOG.debug("Already has version result: " + dr.toString());
				return false;
			}
		}
		return true;
	}

	protected void createVersionResult(XmlMetadata file, String groupId, String artifactId, String versionId, String name) {
		PomVersion vr = new PomVersion();

		// default to artifact ID if name isn't provided.
		if (StringUtils.isBlank(name)) {
			name = artifactId;
		}

		vr.setName(name);
		vr.setVersion(versionId);
		vr.setArchetypeId(artifactId);
		vr.setGroupId(groupId);

		file.getArchiveMeta().getDecorations().add(vr);
		
		if(file.getArchiveMeta() instanceof ZipMetadata) {
			ZipMetadata zip = (ZipMetadata)file.getArchiveMeta();
			knownArchiveProfiler.isKnownVendor(zip, groupId);
		}
		
		
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		XPath xpath = factory.newXPath();
		groupIdXPath = xpath.compile(GROUP_ID);
		artifactIdXPath = xpath.compile(ARTIFACT_ID);
		versionXPath = xpath.compile(VERSION);
		nameXPath = xpath.compile(NAME);

		parentGroupIdXPath = xpath.compile(PARENT_GROUP_ID);
		parentVersionXPath = xpath.compile(PARENT_VERSION);
	}
}
