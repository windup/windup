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
package org.jboss.windup.reporting.transformers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.IntRange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.decoration.AbstractDecoration;
import org.jboss.windup.metadata.decoration.Classification;
import org.jboss.windup.metadata.decoration.Line;
import org.jboss.windup.metadata.decoration.Summary;
import org.jboss.windup.metadata.decoration.AbstractDecoration.NotificationLevel;
import org.jboss.windup.metadata.decoration.archetype.VendorResult;
import org.jboss.windup.metadata.decoration.effort.Effort;
import org.jboss.windup.metadata.decoration.effort.StoryPointEffort;
import org.jboss.windup.metadata.decoration.effort.UnknownEffort;
import org.jboss.windup.metadata.type.FileMetadata;
import org.jboss.windup.metadata.type.ResourceMetadata;
import org.jboss.windup.metadata.type.archive.ArchiveMetadata;
import org.jboss.windup.reporting.ReportUtil;
import org.jboss.windup.reporting.data.ResourceData;
import org.jboss.windup.reporting.html.ArchiveReport;
import org.jboss.windup.reporting.html.ResourceReport;


public abstract class MetaResultTransformer<T extends ResourceMetadata> {

	private static final Log LOG = LogFactory.getLog(MetaResultTransformer.class);
	
	protected void populateResourceData(T meta, File reportDirectory, ResourceData data) {
		if(data == null) {
			throw new IllegalArgumentException("ResourceData must not be null.");
		}
		
		File file = meta.getFilePointer();
		
		data.setSyntax(buildSyntax());
		data.setDecorations(meta.getDecorations());
		data.setRelativePathToRoot(ReportUtil.calculateRelativePathToRoot(reportDirectory, file));
		data.setRelativePathFromRoot(ReportUtil.calculateRelativePathFromRoot(reportDirectory, file));
	}
	
	public ResourceReport toResourceReport(T meta, File reportDirectory, ArchiveReport parent) {
		ResourceReport report = new ResourceReport();
		report.setTitle(buildTitle(meta, reportDirectory));
		report.setSummary(buildSummary(meta));
		populateResourceData(meta, reportDirectory, report);
		report.setEffort((calculateEffort(report.getDecorations())));
		report.setSourceModification(buildSourceModification(meta));
		return report;
	}
	
	
	public ResourceData toResourceData(T meta, File reportDirectory) {
		ResourceData resourceData = new ResourceData();
		populateResourceData(meta, reportDirectory, resourceData);
		
		return resourceData;
	}
	

	protected Effort calculateEffort(Collection<AbstractDecoration> results) {
		int effortCount = 0;
		boolean unknownEffort = false;
		for (AbstractDecoration result : results) {
			if (result.getEffort() instanceof StoryPointEffort) {
				effortCount += ((StoryPointEffort) result.getEffort()).getHours();
			}
			if (result.getEffort() instanceof UnknownEffort) {
				// unknownEffort = true;
				// break;
			}
		}

		if (unknownEffort) {
			return new UnknownEffort();
		}
		return new StoryPointEffort(effortCount);
	}
	
	protected String buildSummary(T meta) {
		StringBuilder summary = new StringBuilder();
		for (AbstractDecoration result : meta.getDecorations()) {
			if (result instanceof Classification) {
				summary.append("Classification: "+result.getDescription() + "<br />");
			}
			if (result instanceof Summary) {
				summary.append(result.getDescription() + "<br />");
			}
		}
		
		return summary.toString();
	}
	
	protected boolean[] buildSourceModification(T meta) {
		List<Line> lrs = new LinkedList<Line>();

		// read file, look for lines...
		for (AbstractDecoration result : meta.getDecorations()) {
			if (result instanceof Line && result.getLevel().isLevel(NotificationLevel.WARNING)) {
				lrs.add((Line) result);
			}
		}

		if (lrs.size() > 0) {
			try {
				int numLines = FileUtils.readLines(meta.getFilePointer()).size();
				int twentyPercent = (int) Math.floor(numLines * 0.2);

				if (LOG.isDebugEnabled()) {
					LOG.debug("File: " + meta.getFilePointer().getAbsolutePath());
					LOG.debug("Number of lines: " + numLines);
					LOG.debug("20 Percent: " + twentyPercent);
				}

				boolean[] isRed = new boolean[] { false, false, false, false, false };
				IntRange[] ranges = new IntRange[5];
				ranges[0] = new IntRange(0, twentyPercent);
				ranges[1] = new IntRange(ranges[0].getMaximumInteger() + 1, ranges[0].getMaximumInteger() + twentyPercent);
				ranges[2] = new IntRange(ranges[1].getMaximumInteger() + 1, ranges[1].getMaximumInteger() + twentyPercent);
				ranges[3] = new IntRange(ranges[2].getMaximumInteger() + 1, ranges[2].getMaximumInteger() + twentyPercent);
				ranges[4] = new IntRange(ranges[3].getMaximumInteger() + 1, numLines);

				for (Line lr : lrs) {
					for (int rIter = 0, rTotal = ranges.length; rIter < rTotal; rIter++) {
						if (ranges[rIter].containsInteger(lr.getLineNumber())) {
							if (LOG.isDebugEnabled()) {
								LOG.debug("Line: " + lr.getLineNumber() + " within range[" + rIter + "]: " + ranges[rIter].toString());
							}
							isRed[rIter] = true;
						}
					}
				}
				
				return isRed;
			}
			catch(IOException e) {
				LOG.error("Exception reading file.", e);
			}
		}
			
		return null;
	}
	
	protected boolean buildVendorResult(ArchiveMetadata meta) {
		for (AbstractDecoration result : meta.getDecorations()) {
			if (result instanceof VendorResult) {
				return true;
			}
		}
		return false;
	}
	
	protected String buildTitle(T meta, File rootDirectory) {
		String title = StringUtils.removeStart(meta.getFilePointer().getAbsolutePath(), rootDirectory.getAbsolutePath());
		title = StringUtils.replace(title, "\\", "/");
		title = StringUtils.removeStart(title, "/");
		
		if(meta instanceof FileMetadata) {
			String starter = ((FileMetadata)meta).getArchiveMeta().getRelativePath();
			
			if(LOG.isDebugEnabled()) {
				LOG.debug("Removing: " + starter + " from "+title);
			}
			
			title = StringUtils.removeStart(title, starter);
		}
		title = StringUtils.removeStart(title, "/");
		
		return title;
	}
	
	protected abstract String buildSyntax();

	protected Effort buildEffort(T meta) {
		Effort effort = this.calculateEffort(meta.getDecorations());
		return effort;
	}

	protected Class<?> transformerType() {
		Class<?> result = null;
		Type type = this.getClass().getGenericSuperclass();

		if (type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) type;
			Type[] fieldArgTypes = pt.getActualTypeArguments();
			result = (Class<?>) fieldArgTypes[0];
		}
		return result;
	}
}
