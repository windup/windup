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
package org.jboss.windup.graph.clz;

import org.jboss.windup.graph.profile.Profile;

public class ProfileClz extends GraphableClz {
	private final Profile profile;
	
	public ProfileClz(String className, Profile profile) {
		super(className);
		this.profile = profile;
	}
	
	public Profile getProfile() {
		return profile;
	}
}
