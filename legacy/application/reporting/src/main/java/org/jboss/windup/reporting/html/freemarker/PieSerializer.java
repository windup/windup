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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.decoration.AbstractDecoration;
import org.jboss.windup.metadata.decoration.Line;
import org.jboss.windup.reporting.html.ArchiveReport;
import org.jboss.windup.reporting.html.ResourceReport;


import freemarker.core.Environment;
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class PieSerializer implements TemplateDirectiveModel {
	private static final String NL = "\n";

	private static final Log LOG = LogFactory.getLog(PieSerializer.class);
	
	@Override
	public void execute(Environment env, Map map, TemplateModel[] templateModel, TemplateDirectiveBody templateDirectiveBody) throws TemplateException, IOException {
		StringModel stringModel = (StringModel)map.get("archive");
		ArchiveReport archiveReport = (ArchiveReport)stringModel.getWrappedObject();
		Map<String, Integer> data = PieSerializer.extractData(archiveReport);
		if (data.keySet().size() > 0) {
			env.getOut().append("<div class='archivePie'>");
			PieSerializer.drawPie(env.getOut(), archiveReport.getRelativePathFromRoot(), data);
			env.getOut().append("</div>");
		}
	}



	public static Map<String, Integer> recursePie(ArchiveReport ar) throws IOException {
		Map<String, Integer> data = new HashMap<String, Integer>();
		recursePie(ar, data);

		return data;
	}
	
	public static void recursePie(ArchiveReport ar, Map<String, Integer> data) throws IOException {
		for(ArchiveReport arr : ar.getNestedArchiveReports()) {
			recursePie(arr, data);
		}
		extractData(ar, data);
	}
	
	public static void drawPie(Writer writer, ArchiveReport ar) throws IOException {
		drawPie(writer, ar.getRelativePathFromRoot(), extractData(ar));
	}

	public static Map<String, Integer> extractData(ArchiveReport ar) {
		Map<String, Integer> data = new HashMap<String, Integer>();
		return extractData(ar, data);
	}
	
	public static Map<String, Integer> extractData(ArchiveReport ar, Map<String, Integer> data) {
		// summarize results.
		String pattern;

		int val = 0;
		String[] keyArray;
		for (ResourceReport report : ar.getResourceReports()) {
			for (AbstractDecoration result : report.getDecorations()) {
				if (result instanceof Line) {
					val = 1;
					pattern = result.getPattern();
					keyArray = pattern.split("\\.");
					pattern = (keyArray.length > 1) ? keyArray[0] + "." + keyArray[1] + ".*" : pattern;
					if (data.containsKey(pattern)) {
						val = data.get(pattern);
						val++;
					}
					data.put(pattern, val);
				}
			}
		}

		return data;
	}

	public static void drawPie(Writer writer, String archiveName, Map<String, Integer> data) throws IOException {
		if (data.size() == 0) {
			// writer.append("<!--No pie.-->");
			return;
		}

		List<PieSort> pieList = topX(data, 9);
		String id = generateId(archiveName);

		String dataId = "data_" + id;
		String pieId = id + "_Pie";
		writer.append("<div id='" + pieId + "' class='windupPieGraph'></div>");
		writer.append("<script type='text/javascript'>");
		writer.append(NL).append("$(function () {");
		writer.append(NL).append("	var " + dataId + " = [];");

		int i = 0;
		for (PieSort p : pieList) {
			writer.append(NL).append("	data_" + id + "[" + i + "] = { label: '" + p.key + "', data: " + p.value + " };");
			i++;
		}
		writer.append(NL).append("	$.plot($('#" + pieId + "'), " + dataId + ", {");
		writer.append(NL).append("		series: {");
		writer.append(NL).append("			pie: {");
		writer.append(NL).append("				show: true");
		writer.append(NL).append("			}");
		writer.append(NL).append("		}");
		writer.append(NL).append("	});");
		writer.append(NL).append("});");

		writer.append(NL).append("</script>");
	}

	private static String generateId(String archiveName) {
		// create uuid for making it unique
		String id = archiveName + UUID.randomUUID();

		// strip all non-alphanumeric characters
		id = id.replaceAll("[^A-Za-z0-9]", "");

		return id;
	}

	private static List<PieSort> topX(Map<String, Integer> map, int top) {
		List<PieSort> list = new ArrayList<PieSort>(map.keySet().size() + 1);
		List<PieSort> bottomList;

		// Add the key/value pairs to the list containing the PieSort(key,value) object.
		for (String key : map.keySet()) {
			PieSort p = new PieSort(key + " - " + map.get(key), map.get(key));
			list.add(p);
		}

		Collections.sort(list);

		// Collect the bottom of the list for the "Other" category.
		int other = 0;
		if (top < list.size()) {
			bottomList = list.subList(top, list.size());

			// Add the "Other" category up.
			for (PieSort p : bottomList) {
				other += p.value;
			}

			list = list.subList(0, top);
		}

		if (other > 0) {
			list.add(new PieSort("Other" + " - " + other, other));
		}

		return list;
	}
}

class PieSort implements Comparable<PieSort> {
	public String key;
	public Integer value;

	public PieSort(String k, Integer v) {
		this.key = k;
		this.value = v;
	}

	@Override
	public int compareTo(PieSort p) {
		if (value < p.value)
			return 1;
		if (value == p.value)
			return 0;
		else
			return -1;
	}
}
