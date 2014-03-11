package org.jboss.windup.engine.visitor;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.jboss.windup.engine.visitor.base.AbstractGraphVisitor;
import org.jboss.windup.graph.dao.ArchiveDaoBean;
import org.jboss.windup.graph.dao.FileResourceDaoBean;
import org.jboss.windup.graph.model.resource.ArchiveResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts the hash values from the archive and sets them on the archive vertex. 
 * 
 * @author bradsdavis@gmail.com
 *
 */
public class ArchiveHashVisitor extends AbstractGraphVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(ArchiveHashVisitor.class);
	
	@Inject
	private FileResourceDaoBean fileDao;
	
	@Inject
	private ArchiveDaoBean archiveDao;
	
	@Override
	public void run() {
		for(ArchiveResource archive : archiveDao.getAll()) {
			visitArchive(archive);
		}
		fileDao.commit();
	}
	
	@Override
	public void visitArchive(ArchiveResource file) {
		InputStream is = null;
		try {
			is = archiveDao.getPayload(file);
			String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(is);
			
			//start over
			is = archiveDao.getPayload(file);
			String sha1 = org.apache.commons.codec.digest.DigestUtils.sha1Hex(is);
			
			file.setSHA1Hash(sha1);
			file.setMD5Hash(md5);
		}
		catch(IOException e) {
			LOG.error("Exception generating hash.", e);
		}
		finally {
			IOUtils.closeQuietly(is);
		}
	}
	
}
