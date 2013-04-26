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
package org.jboss.windup.reporting.html.writer;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.reporting.html.ArchiveReport;
import org.jboss.windup.reporting.html.freemarker.ArchiveReportSummarySerializer;
import org.jboss.windup.reporting.html.freemarker.PieSerializer;
import org.jboss.windup.reporting.html.freemarker.SourceModificationSerializer;
import org.jboss.windup.reporting.html.freemarker.SummaryPieSerializer;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class SummaryHtmlWriter {
	private static final Log LOG = LogFactory.getLog(SummaryHtmlWriter.class);
	
	private final Configuration cfg;

	public SummaryHtmlWriter() {
		cfg = new Configuration();
        cfg.setTemplateUpdateDelay(500);
        cfg.setClassForTemplateLoading(this.getClass(), "/");
	}
	
	public void writeBody(Writer writer, ArchiveReport archiveReport) throws IOException {
		Template template = cfg.getTemplate("/freemarker/summary/summary-report.ftl");
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("pie", new PieSerializer());
		data.put("overviewpie", new SummaryPieSerializer());
		data.put("archivesummary", new ArchiveReportSummarySerializer());
		data.put("modifier", new SourceModificationSerializer());
		data.put("archiveReport", archiveReport);
		
		toWriter(template, writer, data);
	}

	public void writeStatic(Writer writer, ArchiveReport archiveReport) throws IOException {
		Template template = cfg.getTemplate("/freemarker/summary/summary-static.ftl");
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("pie", new PieSerializer());
		data.put("overviewpie", new SummaryPieSerializer());
		data.put("archivesummary", new ArchiveReportSummarySerializer());
		data.put("modifier", new SourceModificationSerializer());
		data.put("archiveReport", archiveReport);
		
        toWriter(template, writer, data);
	}

	protected void toWriter(Template template, Writer writer, Map<String, Object> data) throws IOException {
        try {
			template.process(data, writer);
		} catch (TemplateException e) {
			throw new IOException("Exception writing template.", e);
		}
        finally {
        	writer.close();
        }
	}
}
