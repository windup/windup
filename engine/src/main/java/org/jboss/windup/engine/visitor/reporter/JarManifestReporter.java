package org.jboss.windup.engine.visitor.reporter;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.jboss.windup.graph.dao.JarManifestDaoBean;
import org.jboss.windup.graph.model.meta.JarManifest;
import org.jboss.windup.graph.model.resource.JarArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows all manifest properties.
 * 
 * @author bradsdavis@gmail.com
 *
 */
public class JarManifestReporter extends EmptyGraphVisitor {

	private static final Logger LOG = LoggerFactory.getLogger(JarManifestReporter.class);
	
	@Inject
	private JarManifestDaoBean manifestDao;
	
	@Override
	public void run() {
		for(JarManifest manifest : manifestDao.getAll()) {
			JarArchive archive = manifest.getJarArchive();
			
			LOG.info("Manifest for Archive: "+archive.getArchiveName());
			for(String key : manifest.keySet()) {
				LOG.info("  - "+key+": "+manifest.getProperty(key));
			}
		}
	}
}
