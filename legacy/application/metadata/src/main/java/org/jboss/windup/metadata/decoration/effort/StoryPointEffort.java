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
package org.jboss.windup.metadata.decoration.effort;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="hours-effort")
public class StoryPointEffort extends Effort {
	protected Integer hours;

	public StoryPointEffort() {

	}

	public StoryPointEffort(Integer hours) {
		this.hours = hours;
	}

	public Integer getHours() {
		return hours;
	}

	public void setHours(Integer hours) {
		this.hours = hours;
	}

	@Override
	public String toString() {
		if (hours == null) {
			return "Unknown";
		}
		if (hours == 1) {
			return hours.toString() + " Point";
		}
		return hours.toString() + " Points";
	}
}
