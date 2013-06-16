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

@XmlRootElement(name = "java-line-result")
public class JavaLine extends Line {
	public SourceType getSourceType() {
		return sourceType;
	}

	public void setSourceType(SourceType sourceType) {
		this.sourceType = sourceType;
	}

	private SourceType sourceType;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((sourceType == null) ? 0 : sourceType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof JavaLine))
			return false;
		JavaLine other = (JavaLine) obj;
		if (sourceType != other.sourceType)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "JavaLine [sourceType=" + sourceType + ", getLineNumber()=" + getLineNumber() + ", getHints()="
				+ getHints() + ", getPattern()=" + getPattern() + ", getDescription()=" + getDescription() + ", getLevel()=" + getLevel() + "]";
	}
}
