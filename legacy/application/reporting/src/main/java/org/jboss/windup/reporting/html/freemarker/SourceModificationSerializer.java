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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import freemarker.core.Environment;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class SourceModificationSerializer implements TemplateDirectiveModel {
	private static final Log LOG = LogFactory.getLog(SourceModificationSerializer.class);

	@Override
	public void execute(Environment env, Map map, TemplateModel[] templateModel, TemplateDirectiveBody templateDirectiveBody) throws TemplateException, IOException {
		
		if(!map.containsKey("modification")) {
			return;
		}
		if(map.get("modification")==null) {
			return;
		}
		SimpleSequence stringModel = (SimpleSequence)map.get("modification");
		
		List<Boolean> sourceModification = stringModel.toList();
		
		Writer writer = env.getOut();
		if(sourceModification != null) {
			writer.append("<div class='sourceView'>");
			for (Boolean sourceMod : sourceModification) {
				writer.append("<div class='sourceBlock ");
				if (sourceMod) {
					writer.append("redSourceBlock");
				}
				else {
					writer.append("greenSourceBlock");
				}
				writer.append("'></div>");
			}
			writer.append("</div>");
		}
	}
}
