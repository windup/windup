package org.jboss.windup.engine.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipUtil {
	private static final Logger LOG = LoggerFactory.getLogger(ZipUtil.class);
	
	public static File unzipToTemp(ZipFile file, ZipEntry entry) throws IOException {
		InputStream in = null;
		OutputStream out = null;
		try {
			String entryExtension = StringUtils.substringAfterLast(entry.getName(), ".");
			File temp = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString()+"."+entryExtension);
			in = file.getInputStream(entry);
			out = new FileOutputStream(temp);
			
			IOUtils.copy(in, out);
			LOG.debug("Extracting entry: "+entry.getName()+" to: "+temp.getAbsolutePath());
			return temp;
		}
		catch(Exception e) {
			throw new IOException("Exception extracting entry: "+entry.getName(), e);
		}
		finally {
	        IOUtils.closeQuietly(in);
	        IOUtils.closeQuietly(out);
		}
	}
}
