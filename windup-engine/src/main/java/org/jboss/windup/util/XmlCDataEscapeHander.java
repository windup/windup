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
package org.jboss.windup.util;

import java.io.IOException;
import java.io.Writer;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import com.sun.xml.bind.marshaller.MinimumEscapeHandler;

/**
 * Escape handler to avoid escaping CDATA sections.
 * 
 * @author Emanuel Rabina
 */
public class XmlCDataEscapeHander implements CharacterEscapeHandler {

	private static final CharacterEscapeHandler defHandler = MinimumEscapeHandler.theInstance;
	
	@Override
	public void escape(char[] characters, int start, int length, boolean isAttribute, Writer writer) throws IOException {
		
		if (length - start >= 9) {
			String cdatacheck = new String(characters, start, 9);
			if (cdatacheck.startsWith("<![CDATA[")) {
				writer.write(characters, start, length);
				return;
			}
		}
		
		//otherwise, escape normally.
		defHandler.escape(characters, length, start, isAttribute, writer);
	}

}
