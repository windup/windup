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

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "line-result")
public class Line extends AbstractDecoration {

	private Integer lineNumber;

	public Integer getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(Integer lineNumber) {
		this.lineNumber = lineNumber;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((lineNumber == null) ? 0 : lineNumber.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Line))
			return false;
		Line other = (Line) obj;
		if (lineNumber == null) {
			if (other.lineNumber != null)
				return false;
		}
		else if (!lineNumber.equals(other.lineNumber))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Line [lineNumber=" + lineNumber + "]";
	}
}
