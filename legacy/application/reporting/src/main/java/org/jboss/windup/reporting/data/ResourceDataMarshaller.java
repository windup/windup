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
package org.jboss.windup.reporting.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.jboss.windup.metadata.util.XmlCDataAdapter;
import org.jboss.windup.util.XmlCDataEscapeHander;



public class ResourceDataMarshaller {

	public void marshal(File metaOut, ResourceData data) throws IOException {
		Writer writer = null;
		try {
			writer = new FileWriter(metaOut);
			marshal(writer, data);
		}
		finally {
			if(writer!=null) {
				writer.close();
			}
		}
	}
	
	public void marshal(Writer metaOut, ResourceData data) throws IOException {
        JAXBContext contextA;
		try {
			contextA = JAXBContext.newInstance(ResourceData.class);
			
			Marshaller marshaller = contextA.createMarshaller();
			marshaller.setProperty("com.sun.xml.bind.characterEscapeHandler", new XmlCDataEscapeHander());
			marshaller.marshal(data, metaOut);
		} catch (JAXBException e) {
			throw new IOException("Exception marshalling XML.", e);
		}
	}
	
	public ResourceData unmarshal(Reader metaIn) throws IOException {
        JAXBContext contextA;
		try {
			contextA = JAXBContext.newInstance(ResourceData.class);
			Unmarshaller marshaller = contextA.createUnmarshaller();
			return (ResourceData)marshaller.unmarshal(metaIn);
		} catch (JAXBException e) {
			throw new IOException("Exception marshalling XML.", e);
		}
	}
	
	
}
