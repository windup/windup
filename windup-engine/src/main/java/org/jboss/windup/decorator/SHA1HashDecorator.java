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

import java.io.FileInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.decoration.Hash;
import org.jboss.windup.metadata.decoration.Hash.HashType;
import org.jboss.windup.metadata.type.FileMetadata;


public class SHA1HashDecorator extends ChainingDecorator<FileMetadata> {

	public static final Log LOG = LogFactory.getLog(SHA1HashDecorator.class);

	@Override
	public void processMeta(FileMetadata meta) {
		try {
			FileInputStream fis = new FileInputStream(meta.getFilePointer());
			String sha1 = org.apache.commons.codec.digest.DigestUtils.sha1Hex(fis);

			Hash result = new Hash();
			result.setHashType(HashType.SHA1);
			result.setHash(sha1);

			meta.getDecorations().add(result);

			if (LOG.isDebugEnabled()) {
				LOG.debug("File: " + meta.getFilePointer().getAbsolutePath() + " : SHA1: " + sha1);
			}

			chainDecorators(meta);
		}
		catch (Exception e) {
			LOG.error("Exception generating MD5.", e);
		}
	}

}
