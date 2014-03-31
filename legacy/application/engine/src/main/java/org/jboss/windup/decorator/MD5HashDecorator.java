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


public class MD5HashDecorator extends ChainingDecorator<FileMetadata> {

	private static final Log LOG = LogFactory.getLog(MD5HashDecorator.class);

	@Override
	public void processMeta(FileMetadata meta) {
		try {
			FileInputStream fis = new FileInputStream(meta.getFilePointer());
			String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(fis);

			Hash result = new Hash();
			result.setHashType(HashType.MD5);
			result.setHash(md5);

			meta.getDecorations().add(result);

			if (LOG.isDebugEnabled()) {
				LOG.debug("File: " + meta.getFilePointer().getAbsolutePath() + " : MD5: " + md5);
			}

			chainDecorators(meta);
		}
		catch (Exception e) {
			LOG.error("Exception generating MD5.", e);
		}
	}
}
