package org.jboss.windup.engine.visitor;

import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.jboss.windup.graph.dao.EarArchiveDaoBean;
import org.jboss.windup.graph.dao.JarArchiveDaoBean;
import org.jboss.windup.graph.dao.JarManifestDaoBean;
import org.jboss.windup.graph.dao.WarArchiveDaoBean;
import org.jboss.windup.graph.model.meta.JarManifest;
import org.jboss.windup.graph.model.resource.EarArchive;
import org.jboss.windup.graph.model.resource.JarArchive;
import org.jboss.windup.graph.model.resource.WarArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts manifest information to graph. 
 * 
 * @author bradsdavis@gmail.com
 *
 */
public class ManifestVisitor extends EmptyGraphVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(ManifestVisitor.class);

	@Inject
	private WarArchiveDaoBean warDao;
	
	@Inject
	private JarArchiveDaoBean jarDao;
	
	@Inject
	private EarArchiveDaoBean earDao;
	
	@Inject
	private JarManifestDaoBean manifestDao;
	
	@Override
	public void run() {
		for(JarArchive archive : jarDao.getAll()) {
			visitJarArchive(archive);
		}
		
		for(WarArchive archive : warDao.getAll()) {
			visitJarArchive(archive);
		}
		
		for(EarArchive archive : earDao.getAll()) {
			visitJarArchive(archive);
		}
		
		manifestDao.commit();
	}
	
	@Override
	public void visitJarArchive(JarArchive entry) {
		try {
			JarFile file = jarDao.asJarFile(entry);
			Manifest fileManifest = file.getManifest();
			if(fileManifest == null || fileManifest.getMainAttributes().size() == 0) {
				return;
			}
			
			JarManifest manifest = manifestDao.create();
			manifest.setJarArchive(entry);
			
			for(Object key : fileManifest.getMainAttributes().keySet()) {
				String property = StringUtils.trim(key.toString());
				String propertyValue = StringUtils.trim(fileManifest.getMainAttributes().get(key).toString());
				manifest.setProperty(property, propertyValue);
			}
			
		} catch (IOException e) {
			throw new RuntimeException("Unable to open JAR.", e);
		}
		
	}
	
}
