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
package org.jboss.windup.metadata.decoration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

public class Interrogation implements Comparable<Interrogation> {
	private String summary = "";
	private String title = null;

	private int order = 0;

	private final File result;
	private final File archiveDirectory;
	private final File reportDirectory;

	public Interrogation(File result, File reportDirectory, File archiveDirectory) {
		this.result = result;
		this.reportDirectory = reportDirectory;
		this.archiveDirectory = archiveDirectory;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getSummary() {
		return summary;
	}

	public int getOrder() {
		return order;
	}

	public File getResult() {
		return result;
	}

	@Override
	public int compareTo(Interrogation o) {
		return order - o.getOrder();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((archiveDirectory == null) ? 0 : archiveDirectory.hashCode());
		result = prime * result + order;
		result = prime * result + ((reportDirectory == null) ? 0 : reportDirectory.hashCode());
		result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
		result = prime * result + ((summary == null) ? 0 : summary.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Interrogation other = (Interrogation) obj;
		if (archiveDirectory == null) {
			if (other.archiveDirectory != null)
				return false;
		}
		else if (!archiveDirectory.equals(other.archiveDirectory))
			return false;
		if (order != other.order)
			return false;
		if (reportDirectory == null) {
			if (other.reportDirectory != null)
				return false;
		}
		else if (!reportDirectory.equals(other.reportDirectory))
			return false;
		if (result == null) {
			if (other.result != null)
				return false;
		}
		else if (!result.equals(other.result))
			return false;
		if (summary == null) {
			if (other.summary != null)
				return false;
		}
		else if (!summary.equals(other.summary))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		}
		else if (!title.equals(other.title))
			return false;
		return true;
	}

	public String getTitle() {
		if (StringUtils.isBlank(title)) {
			return getArchiveRelativePath();
		}
		return title;
	}

	public String getLink() {
		return getReportRelativePath();
	}

	protected String getArchiveRelativePath() {
		String basePath = this.archiveDirectory.getAbsolutePath();
		String finalRelative = StringUtils.removeStart(this.result.getAbsolutePath(), basePath);

		finalRelative = StringUtils.replace(finalRelative, "\\", "/");
		finalRelative = StringUtils.replace(finalRelative, "//", "/");
		finalRelative = StringUtils.removeStart(finalRelative, "/");

		return finalRelative;
	}

	protected String getReportRelativePath() {
		String basePath = this.reportDirectory.getAbsolutePath();
		String finalRelative = StringUtils.removeStart(this.result.getAbsolutePath(), basePath);

		finalRelative = StringUtils.replace(finalRelative, "\\", "/");
		finalRelative = StringUtils.replace(finalRelative, "//", "/");
		finalRelative = StringUtils.removeStart(finalRelative, "/");

		return finalRelative;
	}

	public void renderHtml(BufferedWriter bw, boolean oddRow) throws IOException {
		bw.append("<tr ");
		bw.append("class='" + (oddRow ? "rowodd" : "roweven") + "'");
		bw.append(">");

		if (this.result != null) {
			bw.append("<td><a href='" +
					getReportRelativePath() +
					"'>" + getTitle() + "</a></td>");
			bw.append("<td>" + getSummary() + "</td>");
		}
		else {
			// else do not put a link... just the title.
			bw.append("<td>" + getTitle() + "</td>");
			bw.append("<td>" + getSummary() + "</td>");
		}

		bw.append("</tr>");
	}
}
