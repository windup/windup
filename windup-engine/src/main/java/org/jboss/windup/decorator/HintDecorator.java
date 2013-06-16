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

import java.util.List;

import org.jboss.windup.hint.ResultProcessor;
import org.jboss.windup.metadata.decoration.AbstractDecoration;
import org.jboss.windup.metadata.type.FileMetadata;


public class HintDecorator implements MetaDecorator<FileMetadata> {
	protected List<ResultProcessor> hintProcessors;

	public void setHintProcessors(List<ResultProcessor> hintProcessors) {
		this.hintProcessors = hintProcessors;
	}

	public List<ResultProcessor> getHintProcessors() {
		return hintProcessors;
	}

	public void addHintProcessors(List<ResultProcessor> hintProcessors) {
		this.hintProcessors.addAll(hintProcessors);
	}

	@Override
	public void processMeta(FileMetadata file) {
		for (AbstractDecoration dr : file.getDecorations()) {
			for (ResultProcessor hp : hintProcessors) {
				hp.process(dr);
			}
		}
	}
}
