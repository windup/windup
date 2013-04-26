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

import static org.freecompany.redline.header.Header.HeaderTag.HEADERIMMUTABLE;
import static org.freecompany.redline.header.Signature.SignatureTag.SIGNATURES;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.freecompany.redline.ChannelWrapper.Key;
import org.freecompany.redline.ReadableChannelWrapper;
import org.freecompany.redline.header.Format;
import org.freecompany.redline.payload.CpioHeader;

public class RPMToZipTransformer {

	private static Log LOG = LogFactory.getLog(RPMToZipTransformer.class);

	public static File convertRpmToZip(File file) throws Exception {
		LOG.info("File: " + file.getAbsolutePath());

		FileInputStream fis = new FileInputStream(file);
		ReadableChannelWrapper in = new ReadableChannelWrapper(Channels.newChannel(fis));

		InputStream uncompressed = new GZIPInputStream(fis);
		in = new ReadableChannelWrapper(Channels.newChannel(uncompressed));

		String rpmZipName = file.getName();
		rpmZipName = StringUtils.replace(rpmZipName, ".", "-");
		rpmZipName = rpmZipName + ".zip";
		String rpmZipPath = FilenameUtils.getFullPath(file.getAbsolutePath());

		File rpmZipOutput = new File(rpmZipPath + File.separator + rpmZipName);
		LOG.info("Converting RPM: " + file.getName() + " to ZIP: " + rpmZipOutput.getName());
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(rpmZipOutput));

		String rpmName = file.getName();
		rpmName = StringUtils.replace(rpmName, ".", "-");

		CpioHeader header;
		int total = 0;
		do {
			header = new CpioHeader();
			total = header.read(in, total);

			if (header.getFileSize() > 0) {
				BoundedInputStream bis = new BoundedInputStream(uncompressed, header.getFileSize());

				String relPath = FilenameUtils.separatorsToSystem(header.getName());
				relPath = StringUtils.removeStart(relPath, ".");
				relPath = StringUtils.removeStart(relPath, "/");
				relPath = rpmName + File.separator + relPath;
				relPath = StringUtils.replace(relPath, "\\", "/");

				ZipEntry zipEntry = new ZipEntry(relPath);
				zos.putNextEntry(zipEntry);
				IOUtils.copy(bis, zos);
			}
			else {
				final int skip = header.getFileSize();
				if (uncompressed.skip(skip) != skip)
					throw new RuntimeException("Skip failed.");
			}

			total += header.getFileSize();
		}
		while (!header.isLast());

		zos.flush();
		zos.close();

		return rpmZipOutput;
	}

	public static Format readFormat(ReadableChannelWrapper in) throws Exception {
		Format format = new Format();

		Key<Integer> lead = in.start();
		format.getLead().read(in);
		LOG.trace("Lead ended at '" + in.finish(lead) + "'.");

		Key<Integer> signature = in.start();
		int count = format.getSignature().read(in);
		int expected = ByteBuffer.wrap((byte[]) format.getSignature().getEntry(SIGNATURES).getValues(), 8, 4).getInt() / -16;
		LOG.trace("Signature ended at '" + in.finish(signature) + "' and contained '" + count + "' headers (expected '" + expected + "').");

		Key<Integer> header = in.start();
		count = format.getHeader().read(in);
		expected = ByteBuffer.wrap((byte[]) format.getHeader().getEntry(HEADERIMMUTABLE).getValues(), 8, 4).getInt() / -16;
		LOG.trace("Header ended at '" + in.finish(header) + " and contained '" + count + "' headers (expected '" + expected + "').");

		return format;
	}
}
