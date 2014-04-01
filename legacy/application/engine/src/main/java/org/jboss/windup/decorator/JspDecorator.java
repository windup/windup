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
package org.jboss.windup.decorator;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.hint.ResultProcessor;
import org.jboss.windup.metadata.decoration.Line;
import org.jboss.windup.metadata.type.JspMetadata;
import org.jboss.windup.util.NewLineUtil;

public class JspDecorator implements MetaDecorator<JspMetadata> {
	private static final Log LOG = LogFactory.getLog(JspDecorator.class);

	protected List<ResultProcessor> hints = new LinkedList<ResultProcessor>();
	protected Set<Pattern> classBlacklistPatterns;
	protected Set<Pattern> namespaceBlacklistPatterns;

	protected static final Pattern jspImport = Pattern.compile("<%@\\s*page\\s+[^>]*\\s*import\\s*=\\s*['\"]([^'\"]+)['\"].*?%>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	protected static final Pattern jspTagLib = Pattern.compile("<%@\\s*taglib\\s+[^>]*\\s*uri\\s*=\\s*['\"]([^'\"]+)['\"].*?%>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	protected static final Pattern jsp2DeprecatedQuotes = Pattern.compile("([\\w\\-0-9]+\\s*?=\\s*?\"\\s*?<%=[\\s\\w\\.\\(]*?[^\\\\]?\"[^%>]+?[^\\\\]\"[\\w\\s\\.\\)]*?%>\\s*?\")(?=.*?>)", Pattern.CASE_INSENSITIVE);

	public void setHints(List<ResultProcessor> hints) {
		this.hints = hints;
	}

	@Override
	public void processMeta(JspMetadata meta) {
		try {
			String jspContents = FileUtils.readFileToString(meta.getFilePointer());
			findImports(jspContents, jspImport, meta);
			findTaglib(jspContents, jspTagLib, meta);
			// findDeprecatedJspQuotes(jspContents, jsp2DeprecatedQuotes, meta);
		}
		catch (IOException e) {
			LOG.error("Exception with Regular Expressions.", e);
		}
	}

	/**
	 * Detects deprecated jsp quote syntax e.g
	 * <tags:tag attr1="<%="hello world"%>" attr2="<%=\"valid\"%>" attr3="<%= print.me("hello world") %>" />
	 * attr1 and attr3 are invalid syntax
	 * 
	 * @param html
	 *            The content of the jsp
	 * @param thePattern
	 *            The regular expression
	 * @param meta
	 *            Meta data for the results
	 */
	protected void findDeprecatedJspQuotes(String html, Pattern thePattern, JspMetadata meta) {
		Matcher matcher = thePattern.matcher(html);
		while (matcher.find()) {
			String matched = matcher.group(1);

			if (StringUtils.isNotBlank(matched)) {
				Line lr = new Line();
				lr.setDescription("Deprecated jsp syntax: " + matched);
				lr.setLineNumber(NewLineUtil.countNewLine(html, matcher.start()));
				lr.setPattern(matched);

				for (ResultProcessor hint : hints) {
					hint.process(lr);
				}
				meta.getDecorations().add(lr);
			}

		}
	}

	protected void findImports(String html, Pattern thePattern, JspMetadata meta) {
		Matcher matcher = thePattern.matcher(html);
		while (matcher.find()) {
			String matched = matcher.group(1);
			if (StringUtils.isNotBlank(matched)) {
				String[] imports = StringUtils.split(matched, ",");
				if (imports != null) {
					for (String imp : imports) {
						imp = StringUtils.trim(imp);
						if (lineContainsClassBlacklist(imp)) {
							Line lr = new Line();
							lr.setDescription("Blacklist: " + imp);
							lr.setLineNumber(NewLineUtil.countNewLine(html, matcher.start()));
							lr.setPattern(imp);

							for (ResultProcessor hint : hints) {
								hint.process(lr);
							}
							meta.getDecorations().add(lr);
						}
						meta.getClassDependencies().add(imp);
					}
				}
			}
		}
	}

	protected void findTaglib(String html, Pattern thePattern, JspMetadata meta) {
		Matcher matcher = thePattern.matcher(html);
		while (matcher.find()) {
			String matched = matcher.group(1);
			if (StringUtils.isNotBlank(matched)) {
				if (lineContainsNamespaceBlacklist(matched)) {
					Line lr = new Line();
					lr.setDescription("Blacklist Namespace: " + matched);
					lr.setLineNumber(NewLineUtil.countNewLine(html, matcher.start()));
					lr.setPattern(matched);

					for (ResultProcessor hint : hints) {
						hint.process(lr);
					}
					meta.getDecorations().add(lr);
				}
				meta.getTaglibDependencies().add(matched);
			}
		}
	}

	public void setClassBlacklists(List<String> classBls) {
		if (classBls == null)
			return;

		this.classBlacklistPatterns = new HashSet<Pattern>(classBls.size());
		for (String classBlacklist : classBls) {
			if (!StringUtils.startsWith(classBlacklist, "^")) {
				classBlacklist = "^" + classBlacklist;
			}
			this.classBlacklistPatterns.add(Pattern.compile(classBlacklist));
		}
	}

	public void setNamespaceBlacklists(List<String> namespaceBls) {
		if (namespaceBls == null)
			return;

		this.namespaceBlacklistPatterns = new HashSet<Pattern>(namespaceBls.size());
		for (String namespaceBlacklist : namespaceBls) {
			if (!StringUtils.startsWith(namespaceBlacklist, "^")) {
				namespaceBlacklist = "^" + namespaceBlacklist;
			}
			this.namespaceBlacklistPatterns.add(Pattern.compile(namespaceBlacklist));
		}
	}

	private boolean lineContainsClassBlacklist(String line) {
		if (line == null)
			return false;

		for (Pattern black : this.classBlacklistPatterns) {
			if (black.matcher(line).find()) {
				return true;
			}
		}
		return false;
	}

	private boolean lineContainsNamespaceBlacklist(String line) {
		if (line == null)
			return false;

		for (Pattern black : this.namespaceBlacklistPatterns) {
			if (black.matcher(line).find()) {
				return true;
			}
		}
		return false;
	}
}
