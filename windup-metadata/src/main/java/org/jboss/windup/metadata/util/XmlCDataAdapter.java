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
package org.jboss.windup.metadata.util;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Special adapter class to marshall certain sections as CDATA text instead of
 * escaped text.
 * 
 * @author Emanuel Rabina
 */

public class XmlCDataAdapter extends XmlAdapter<String, String> {
	

		/**
		 * Wraps <tt>v</tt> in a CDATA section.
		 * 
		 * @param v
		 * @return <tt>&lt;![CDATA[ + v + ]]&gt;</tt>
		 */
		@Override
		public String marshal(String v) {

			return "<![CDATA[" + v + "]]>";
		}

		/**
		 * Nothing special, returns <tt>v</tt> as is.
		 * 
		 * @param v
		 * @return <tt>v</tt>
		 */
		@Override
		public String unmarshal(String v) {

			return v;
		}
}
