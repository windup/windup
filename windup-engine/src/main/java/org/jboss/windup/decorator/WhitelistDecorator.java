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
package org.jboss.windup.decorator;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.hint.MatchingProcessor;
import org.jboss.windup.metadata.decoration.AbstractDecoration;
import org.jboss.windup.metadata.type.FileMetadata;


public class WhitelistDecorator implements MetaDecorator<FileMetadata> {
	private static final Log LOG = LogFactory.getLog(WhitelistDecorator.class);

	protected List<MatchingProcessor> whitelistProcessors;

	public void setWhitelistProcessors(List<MatchingProcessor> whitelistProcessors) {
		this.whitelistProcessors = whitelistProcessors;
	}

	@Override
	public void processMeta(FileMetadata file) {
		List<AbstractDecoration> retractList = new LinkedList<AbstractDecoration>();

		for (AbstractDecoration dr : file.getDecorations()) {
			for (MatchingProcessor hp : whitelistProcessors) {
				if (hp.process(dr)) {
					// retract from the decorations.
					retractList.add(dr);
				}
			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Retracting: " + retractList.size() + " decorations.");
		}

		// method used to avoid concurrent modification exception.
		for (AbstractDecoration dr : retractList) {
			boolean retracted = file.getDecorations().remove(dr);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Retracting: " + dr.toString() + " :: " + retracted);
			}
		}
	}
}
