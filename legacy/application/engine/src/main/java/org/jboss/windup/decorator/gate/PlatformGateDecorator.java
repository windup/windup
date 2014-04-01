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
package org.jboss.windup.decorator.gate;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jboss.windup.metadata.type.FileMetadata;
import org.jboss.windup.platform.PlatformType;


public class PlatformGateDecorator extends GateDecorator<FileMetadata> {
	private static final Logger LOG = LoggerFactory.getLogger(PlatformGateDecorator.class);

	private PlatformType targetContainer = null;
	private Set<PlatformType> supportedContainers;

	public void setSupportedContainers(Set<PlatformType> supportedContainers) {
		this.supportedContainers = supportedContainers;
	}

	public void setTargetContainer(PlatformType targetContainer) {
		this.targetContainer = targetContainer;
	}

	@Override
	protected boolean continueProcessing(FileMetadata meta) {
		if (targetContainer == null) {
			LOG.debug("No target specified; run for all target containers.");
			return true;
		}
		for (PlatformType supported : supportedContainers) {
			if (supported.is(targetContainer))
			{
				return true;
			}
		}

		return false;

	}

}
