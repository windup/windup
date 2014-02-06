package org.jboss.windup.engine.visitor;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.engine.util.ZipUtil;
import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.jboss.windup.graph.dao.ArchiveDaoBean;
import org.jboss.windup.graph.dao.FileDaoBean;
import org.jboss.windup.graph.model.resource.Archive;
import org.jboss.windup.graph.model.resource.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes untyped archives, and casts them to the appropriate type within the graph.
 * For nested archives, this also extracts the nested archive for profiling.
 * 
 * @author bradsdavis
 *
 */
public class ZipArchiveGraphVisitor extends EmptyGraphVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(ZipArchiveGraphVisitor.class);
	
	@Inject
	private FileDaoBean fileDao;
	
	@Inject
	private ArchiveDaoBean archiveDao;
	

	private Set<String> getZipExtensions() {
		Set<String> extensions = new HashSet<String>();
		extensions.add(".war");
		extensions.add(".ear");
		extensions.add(".jar");
		extensions.add(".sar");
		extensions.add(".rar");
		
		return extensions;
	}
	
	
	private boolean endsWithExtension(String path) {
		for(String extension : getZipExtensions()) {
			if(StringUtils.endsWith(path, extension)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void visit() {
		//feed all file listeners...
		for(File file : fileDao.findArchiveEntryWithExtension(
				"war", "ear", "jar", "sar", "rar")) {
			visitFile(file);
		}
		fileDao.commit();
	}
	
	@Override
	public void visitFile(File file) {
		//now, check to see whether it is a JAR, and republish the typed value.
		String filePath = file.getFilePath();
		
		if(endsWithExtension(filePath)) {
			java.io.File reference = new java.io.File(filePath);
			ZipFile zipFile = null;
			try {
				zipFile = new ZipFile(reference);
				LOG.info(filePath);
				
				//go ahead and make it into an archive.
				Archive archive = archiveDao.castToType(file);
				LOG.info("Cast vertex: "+file.asVertex()+" to archive.");
				
				//first, make the file reference.
				Enumeration<?> entries = zipFile.entries();
				while(entries.hasMoreElements()) {
					ZipEntry entry = (ZipEntry)entries.nextElement();
					//skip.
					if(entry.isDirectory()) {
						continue;
					}
					if(endsWithExtension(entry.getName())) {
						//unzip.
						java.io.File tempFile = ZipUtil.unzipToTemp(zipFile, entry);
						org.jboss.windup.graph.model.resource.File tempReference = fileDao.getByFilePath(tempFile.getAbsolutePath());
						archiveDao.castToType(tempReference);
						LOG.info("File reference: "+tempReference.asVertex());
						//add the element as a child..
						archive.addChild(tempReference);
					}
				}
			} catch (Exception e) {
				LOG.error("Exception creating zip from: "+filePath, e);
			}
			finally {
				IOUtils.closeQuietly(zipFile);
			}
		}
	
	}

}
