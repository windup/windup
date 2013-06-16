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
package org.jboss.windup.reporting.html.freemarker;

import java.io.IOException;
import java.io.Writer;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.decoration.AbstractDecoration;
import org.jboss.windup.metadata.decoration.Link;
import org.jboss.windup.metadata.decoration.Summary;
import org.jboss.windup.metadata.decoration.archetype.JVMBuildVersionResult;
import org.jboss.windup.metadata.decoration.archetype.VendorResult;
import org.jboss.windup.metadata.decoration.archetype.version.PomVersion;
import org.jboss.windup.metadata.decoration.archetype.version.Version;
import org.jboss.windup.reporting.html.ArchiveReport;

import freemarker.core.Environment;
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class ArchiveReportSummarySerializer implements TemplateDirectiveModel {
	private static final Log LOG = LogFactory.getLog(ArchiveReportSummarySerializer.class);
	
	@Override
	public void execute(Environment env, Map map, TemplateModel[] templateModel, TemplateDirectiveBody templateDirectiveBody) throws TemplateException, IOException {
		StringModel stringModel = (StringModel)map.get("archive");
		ArchiveReport archive = (ArchiveReport)stringModel.getWrappedObject();
		
		
		Writer bw = env.getOut();
	
		String jvmVersion = null;
		String vendor = null;
		Version version = null;
		String summary = null;

		// eliminate links to same place..
		Set<Link> links = new TreeSet<Link>(new Comparator<Link>() {
			@Override
			public int compare(Link o1, Link o2) {
				String link1 = StringUtils.removeEnd(o1.getLink(), "/");
				String link2 = StringUtils.removeEnd(o2.getLink(), "/");

				if (StringUtils.equals(link1, link2)) {
					return 0;
				}
				if (o1 == null || o2 == null) {
					return 1;
				}

				return o1.getLink().compareTo(o2.getLink());
			}
		});
		
		if (archive.getDecorations() != null) {
			for (AbstractDecoration dr : archive.getDecorations()) {
				if (dr == null) {
					LOG.info("Skipping null decorator.");
					continue;
				}
				if (dr instanceof VendorResult) {
					vendor = ((VendorResult) dr).toString();
				}
				if (dr instanceof Version) {
					if ((version != null && version instanceof PomVersion)) {
						// do nothing.
					}
					else if (version == null) {
						version = ((Version) dr);
					}
					else {
						version = ((Version) dr);
					}
				}
				if (dr instanceof Summary) {
					summary = ((Summary) dr).getDescription();
				}
				if (dr instanceof Link) {
					links.add((Link) dr);
				}
				if (dr instanceof JVMBuildVersionResult) {
					jvmVersion = ((JVMBuildVersionResult) dr).getJdkBuildVersion();
				}
			}
		}
		
		StringBuilder builder = new StringBuilder();
		if (StringUtils.isNotBlank(vendor)) {
			builder.append("<div class='archiveSummaryPart'><h3>Vendor</h3><p>").append(vendor).append("</p></div>");
		}
		if (version != null) {
			builder.append("<div class='archiveSummaryPart'><h3>Version</h3><p>").append(version.toString()).append("</p></div>");
		}
		
		if (links.size() > 0)
		{
			for (Link link : links) {
				builder.append("<div class='archiveSummaryPart'><h3>Link</h3><p><a href='" + link.getLink() + "'>" + link.getDescription() + "</p></a></div>");
			}
		}
		if (jvmVersion != null && builder.toString().length() > 0) {
			builder.append("<div class='archiveSummaryPart'><h3>JVM</h3><p>" + "<span class='jreVersion'>" + jvmVersion + "</span></div>");
		}
		if (StringUtils.isNotBlank(summary)) {
			builder.append("<div class='archiveSummaryPart'><h3>Description</h3><p>" + summary + "</p></div>");
		}
		
		String body = builder.toString();
		if(body.length() > 0) {
			bw.append("<div class='archiveSummary'>");
			bw.append(body);
			bw.append("</div>");
		}
	
	}

}
