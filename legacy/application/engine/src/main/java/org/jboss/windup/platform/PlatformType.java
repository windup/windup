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
package org.jboss.windup.platform;

public enum PlatformType {
	SERVER(null),
		EAP6(SERVER),
			EPP6(EAP6),
			SOA6(EAP6),
		EAP5(SERVER),
			EPP5(EAP5),
			SOA5(EAP5),
		JBOSS4(SERVER),
			EPP4(JBOSS4),
			SOA4(JBOSS4),
			BRMS4(JBOSS4);

	private PlatformType parent = null;

	private PlatformType(PlatformType parent) {
		this.parent = parent;
	}

	public boolean is(PlatformType other) {
		if (other == null) {
			return false;
		}

		for (PlatformType t = this; t != null; t = t.parent) {
			if (other == t) {
				return true;
			}
		}
		return false;
	}
}
