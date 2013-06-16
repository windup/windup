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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.jboss.windup.metadata.decoration.effort.Effort;
import org.jboss.windup.metadata.decoration.effort.UnknownEffort;
import org.jboss.windup.metadata.decoration.hint.Hint;

@XmlRootElement(name = "decorator-result")
@XmlSeeAlso({Classification.class, Global.class, Hash.class, JavaLine.class, Line.class, Link.class, Summary.class})
public abstract class AbstractDecoration implements Comparable<AbstractDecoration> {

	protected final Map<String, Object> context = new HashMap<String, Object>();
	protected Effort effort = new UnknownEffort();
	protected Set<Hint> hints = new HashSet<Hint>();
	protected String description;
	protected NotificationLevel level = NotificationLevel.WARNING;
	protected String pattern;

	public Map<String, Object> getContext() {
		return context;
	}
	
	public Set<Hint> getHints() {
		return hints;
	}

	public void setHints(Set<Hint> hints) {
		this.hints = hints;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public AbstractDecoration() {

	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public NotificationLevel getLevel() {
		return level;
	}

	public void setLevel(NotificationLevel level) {
		this.level = level;
	}

	public enum NotificationLevel {

		INFO(0), WARNING(1), SEVERE(2), CRITICAL(3);

		private final int level;

		private NotificationLevel(int level) {
			this.level = level;
		}

		public int getLevel() {
			return level;
		}

		public boolean isLevel(NotificationLevel test) {
			return (this.getLevel() >= test.getLevel());
		}
	}

	public void setEffort(Effort effort) {
		this.effort = effort;
	}

	public Effort getEffort() {
		return effort;
	}

	public int compareTo(AbstractDecoration dr) {
		if (this.equals(dr)) {
			return 0;
		}
		if (this.getDescription() == null) {
			return -1;
		}
		if (dr.getDescription() == null) {
			return 1;
		}
		int compare = this.getDescription().compareToIgnoreCase(dr.getDescription());
		if (compare != 0) {
			return compare;
		}
		return 1;
	}
}
