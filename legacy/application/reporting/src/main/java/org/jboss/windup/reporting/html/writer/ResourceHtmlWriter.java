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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.decoration.AbstractDecoration;
import org.jboss.windup.metadata.decoration.Classification;
import org.jboss.windup.metadata.decoration.Global;
import org.jboss.windup.metadata.decoration.Line;
import org.jboss.windup.metadata.decoration.Link;
import org.jboss.windup.reporting.data.ResourceData;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class ResourceHtmlWriter {
	private static final Log LOG = LogFactory.getLog(ClassloaderHtmlWriter.class);
	
	private final Configuration cfg;

	public ResourceHtmlWriter() {
		cfg = new Configuration();
        cfg.setTemplateUpdateDelay(500);
        cfg.setClassForTemplateLoading(this.getClass(), "/");
	}
	
	public void writeBody(Writer writer, String sourceText, ResourceData meta) throws IOException {
		Template template = cfg.getTemplate("/freemarker/resource/resource-report.ftl");
		
		Map<String, Object> data = new HashMap<String, Object>();
		organizeDecorations(meta, data);
		data.put("snippet", meta.getSyntax());
		data.put("sourceText", sourceText);
		
		toWriter(template, writer, data);
	}

	public void writeStatic(Writer writer, String sourceText, ResourceData meta) throws IOException {
		Template template = cfg.getTemplate("/freemarker/resource/resource-static.ftl");
		
		Map<String, Object> data = new HashMap<String, Object>();
		organizeDecorations(meta, data);
		data.put("snippet", meta.getSyntax());
		data.put("relativePath", meta.getRelativePathToRoot());
        data.put("sourceText", sourceText);
        
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

	private void organizeDecorations(ResourceData meta, Map<String, Object> data) {
		List<Global> globalResults = new LinkedList<Global>();
        List<Classification> classificationResults = new LinkedList<Classification>();
        List<Link> linkResults = new LinkedList<Link>();
        List<Line> lineNumberSuggestions = new LinkedList<Line>();
        String blockSettings = "";
        
        Comparator<Line> lineResult = new Comparator<Line>() {
			
			public int compare(Line arg0, Line arg1) {
				if(arg0.getLineNumber() == null) {
					return -1;
				}
				if(arg1.getLineNumber() == null) {
					return 1;
				}
				
				return arg0.getLineNumber() - arg1.getLineNumber();
			}
		};
		
		
        for(AbstractDecoration dr : meta.getDecorations()) {
        	if(dr instanceof Global) {
        		globalResults.add((Global) dr);
        	}
        	
        	if(dr instanceof Classification) {
        		classificationResults.add((Classification) dr);
        	}
        	
        	if(dr instanceof Line) {
        		Line lr = (Line)dr;
        		if(lr.getLineNumber() == null) {
        			LOG.warn("Line number is null.");
        			continue;
        		}
        		if(StringUtils.isNotBlank(blockSettings)) {
        			blockSettings+=",";
        		}
        		blockSettings+=lr.getLineNumber();
        		
        		lineNumberSuggestions.add(lr);
        	}
        	
        	if(dr instanceof Link) {
        		linkResults.add((Link)dr);
        	}
        	
        }
        Collections.sort(lineNumberSuggestions, lineResult);
        
        data.put("globalResults", globalResults);
        data.put("lineNumberSuggestions", lineNumberSuggestions);
        data.put("classificationResults", classificationResults);
        data.put("linkResults", linkResults);
        data.put("blockSetting", blockSettings);
	}
}









