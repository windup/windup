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
package org.jboss.windup.config.spring.property;

import java.beans.PropertyEditorSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.decoration.effort.StoryPointEffort;


public class StoryPointEffortTypeEditor extends PropertyEditorSupport {
	private static final Log LOG = LogFactory.getLog(StoryPointEffortTypeEditor.class);

	public void setAsText(String hours) {
		if (hours != null)
		{
			int h = Integer.parseInt(hours);
			StoryPointEffort effort = new StoryPointEffort(h);
			setValue(effort);
		}
	}

	@Override
	public void setValue(Object value) {
		if (value instanceof Integer) {
			value = new StoryPointEffort((Integer) value);
		}

		super.setValue(value);
	}
}
