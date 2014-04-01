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

@XmlRootElement(name="link-result")
public class Link extends AbstractDecoration {
	protected String link;

	public String getLink() {
		return link;
	}	

	public void setLink(String link) {
		this.link = link;
	}

}
