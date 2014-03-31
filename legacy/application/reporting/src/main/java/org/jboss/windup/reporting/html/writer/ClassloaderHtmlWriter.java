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
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.reporting.html.ClasspathReport;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class ClassloaderHtmlWriter {
	private static final Log LOG = LogFactory.getLog(ClassloaderHtmlWriter.class);
	
	private final Configuration cfg;

	public ClassloaderHtmlWriter() {
		cfg = new Configuration();
        cfg.setTemplateUpdateDelay(500);
        cfg.setClassForTemplateLoading(this.getClass(), "/");
	}
	
	public void writeBody(Writer writer, String sourceText, List<String> meta) throws IOException {
		Template template = cfg.getTemplate("/freemarker/classloader/classloader-report.ftl");
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("classpathIssue", meta);
		toWriter(template, writer, data);
	}

	public void writeStatic(Writer writer, ClasspathReport classloaderIssues) throws IOException {
		Template template = cfg.getTemplate("/freemarker/classloader/classloader-static.ftl");
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("classpathIssues", classloaderIssues.getMissingToAffected());
		data.put("relativePath", classloaderIssues.getRelativePathToRoot());
        
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
