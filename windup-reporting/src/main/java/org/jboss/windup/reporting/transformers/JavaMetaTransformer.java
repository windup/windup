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
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.decoration.AbstractDecoration;
import org.jboss.windup.metadata.decoration.Classification;
import org.jboss.windup.metadata.decoration.Global;
import org.jboss.windup.metadata.decoration.JavaLine;
import org.jboss.windup.metadata.decoration.AbstractDecoration.NotificationLevel;
import org.jboss.windup.metadata.type.JavaMetadata;


public class JavaMetaTransformer extends GenericMetaTransformer<JavaMetadata> {
	private static final Log LOG = LogFactory.getLog(JavaMetaTransformer.class);

	protected static int calcTotalChanges(JavaMetadata result) {
		int totalChanges = 0;
		Set<String> blkTemp = new HashSet<String>();
		for (AbstractDecoration dr : result.getDecorations()) {
			if (dr instanceof JavaLine) {
				blkTemp.add("Blacklisted: " + dr.getPattern());
				totalChanges++;
			}
		}
		return totalChanges;
	}

	@Override
	protected String buildSyntax() {
		return "java";
	}
	
	@Override
	protected String buildTitle(JavaMetadata meta, File rootDirectory) {
		return meta.getQualifiedClassName();
	}
	
	@Override
	protected String buildSummary(JavaMetadata meta) {
		StringBuilder description = new StringBuilder();
		int totalChanges = 0;
		Set<AbstractDecoration> blkTemp = new TreeSet<AbstractDecoration>();
		for (AbstractDecoration dr : meta.getDecorations()) {
			if (dr instanceof Classification) {
				description.append("Classification: " + dr.getDescription() + "<br />");
			}
			if (dr instanceof Global) {
				description.append(dr.getDescription() + "<br />");
			}
			if (dr instanceof JavaLine) {
				if (dr.getLevel().isLevel(NotificationLevel.WARNING)) {
					blkTemp.add(dr);
				}
			}
		}

		if (blkTemp.size() > 0) {
			description.append("<b>Warnings: " + blkTemp.size() + " items</b>");
			description.append("<ul class='notifications'>");
			for (AbstractDecoration blkList : blkTemp) {
				description.append("<li class='" + blkList.getLevel().toString().toLowerCase() + "'>" + blkList.getPattern() + "</li>");
			}
			description.append("</ul>");
		}
		if (totalChanges > 0) {
			description.append("Estimated Code Changes: " + totalChanges);
		}
		return description.toString();
	}
}
