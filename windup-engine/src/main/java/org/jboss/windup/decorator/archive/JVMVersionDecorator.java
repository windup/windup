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
package org.jboss.windup.decorator.archive;

import java.io.DataInputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;

import javassist.bytecode.ClassFile;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.decorator.MetaDecorator;
import org.jboss.windup.metadata.decoration.archetype.JVMBuildVersionResult;
import org.jboss.windup.metadata.type.archive.ZipMetadata;


public class JVMVersionDecorator implements MetaDecorator<ZipMetadata> {
	private static final Log LOG = LogFactory.getLog(JVMVersionDecorator.class);

	@Override
	public void processMeta(ZipMetadata meta) {
		try {
			ZipEntry entry;
			Enumeration<?> e = meta.getZipFile().entries();
			// locate a random class entry...
			while (e.hasMoreElements()) {
				entry = (ZipEntry) e.nextElement();

				if (StringUtils.endsWith(entry.getName(), ".class")) {
					String version = null;
					DataInputStream in = null;
					try {
						in = new DataInputStream(meta.getZipFile().getInputStream(entry));

						// ignore Java "magic" number.
						in.readInt();
						int minor = in.readUnsignedShort();
						int major = in.readUnsignedShort();

						switch (major) {
						case ClassFile.JAVA_1:
							version = "1.1";
							break;
						case ClassFile.JAVA_2:
							version = "1.2";
							break;
						case ClassFile.JAVA_3:
							version = "1.3";
							break;
						case ClassFile.JAVA_4:
							version = "1.4";
							break;
						case ClassFile.JAVA_5:
							version = "5.0";
							break;
						case ClassFile.JAVA_6:
							version = "6.0";
							break;
						case ClassFile.JAVA_7:
							version = "7.0";
							break;
						default:
							LOG.warn("No version mapping for: " + version);
						}
						version = version + "." + minor;
					}
					finally {
						in.close();
					}
					JVMBuildVersionResult vr = new JVMBuildVersionResult();
					vr.setJdkBuildVersion(version);
					meta.getDecorations().add(vr);

					if (LOG.isDebugEnabled()) {
						LOG.debug("Built with: " + version);
					}
					break;
				}
			}
		}
		catch (Exception e) {
			LOG.error("Exception getting JDK version.", e);
		}
	}
}
