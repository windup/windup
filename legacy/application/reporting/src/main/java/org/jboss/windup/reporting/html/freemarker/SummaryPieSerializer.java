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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.decoration.effort.StoryPointEffort;
import org.jboss.windup.reporting.html.ArchiveReport;
import org.jboss.windup.reporting.html.ResourceReport;


import freemarker.core.Environment;
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class SummaryPieSerializer implements TemplateDirectiveModel {

	private static final Log LOG = LogFactory.getLog(SummaryPieSerializer.class);
	
	@Override
	public void execute(Environment env, Map map, TemplateModel[] templateModel, TemplateDirectiveBody templateDirectiveBody) throws TemplateException, IOException {
		StringModel stringModel = (StringModel)map.get("archive");
		ArchiveReport archiveReport = (ArchiveReport)stringModel.getWrappedObject();
		Map<String, Integer> data = PieSerializer.recursePie(archiveReport);
		if (data.keySet().size() > 0) {
			env.getOut().append("<div class='rightColumn'>");
				env.getOut().append("<div class='windupPieGraph'>");
				PieSerializer.drawPie(env.getOut(), archiveReport.getRelativePathFromRoot(), data);
				env.getOut().append("</div>");
			env.getOut().append("</div>");
		}
		
		draw(env.getOut(), archiveReport);
	}

	
	
	protected void draw(Writer writer, ArchiveReport report) throws IOException {
		StoryPointEffort effort = new StoryPointEffort(0);
		recurseEffort(report, effort);
		writer.write("<div class='leftColumn'><div class='totalSummary'><div class='totalLoe'>"+effort.getHours()+"</div><div class='totalDesc'>Story Points</div></div></div>");
	}
	
	protected void recurseEffort(ArchiveReport ar, StoryPointEffort hours) {
		
		for(ArchiveReport recurse : ar.getNestedArchiveReports()) {
			recurseEffort(recurse, hours);
		}
		
		for(ResourceReport report : ar.getResourceReports()) {
			if(report.getEffort() instanceof StoryPointEffort) {
				Integer h = hours.getHours();
				h += ((StoryPointEffort)report.getEffort()).getHours();
				hours.setHours(h);
			}
		}
	}
	
}
