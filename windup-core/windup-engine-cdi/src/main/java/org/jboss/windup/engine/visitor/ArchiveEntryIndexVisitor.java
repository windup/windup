package org.jboss.windup.engine.visitor;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.jboss.windup.graph.dao.ArchiveDaoBean;
import org.jboss.windup.graph.dao.ArchiveEntryDaoBean;
import org.jboss.windup.graph.dao.FileDaoBean;
import org.jboss.windup.graph.model.resource.Archive;
import org.jboss.windup.graph.model.resource.ArchiveEntryResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Goes through an archive adding the archive entries to the graph.
 * 
 * @author bradsdavis
 *
 */
public class ArchiveEntryIndexVisitor extends EmptyGraphVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(ArchiveEntryIndexVisitor.class);

	@Inject
	FileDaoBean fileDao;
	
	@Inject
	private ArchiveDaoBean archiveDao;

	@Inject
	private ArchiveEntryDaoBean archiveEntryDao;
	
	@Override
	public void visit() {
		for(Archive archive : archiveDao.getAll()) {
			visitArchive(archive);
			archiveEntryDao.commit();
		}
	}
	
	@Override
	public void visitArchive(Archive file) {
		LOG.info("Processing: "+file.asVertex());
		ZipFile zipFileReference = null;
		try {
			File zipFile = new File(file.getFilePath());
			zipFileReference = new ZipFile(zipFile);
			Enumeration<? extends ZipEntry> entries = zipFileReference.entries();
			
			while(entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if(entry.isDirectory()) {
					continue;
				}
				//creates a new archive entry.
				ArchiveEntryResource resource = archiveEntryDao.create(null);
				resource.setArchiveEntry(entry.getName());
				resource.setArchive(file);
			}
		} catch (IOException e) {
			LOG.error("Exception while reading JAR.", e);
		}
		finally {
			org.apache.commons.io.IOUtils.closeQuietly(zipFileReference);
		}
	}
}
