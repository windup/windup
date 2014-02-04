package org.jboss.windup.engine.visitor;

import javax.inject.Inject;

import org.jboss.windup.engine.WindupContext;
import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.jboss.windup.graph.dao.JarArchiveDao;
import org.jboss.windup.graph.model.resource.JarArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArchiveDependsOnReporter extends EmptyGraphVisitor {

	private static final Logger LOG = LoggerFactory.getLogger(ArchiveDependsOnReporter.class);
	
	@Inject
	private JarArchiveDao jarDao;
	
	@Override
	public void visitContext(WindupContext context) {
		for(JarArchive archive : jarDao.getAll()) {
			LOG.info("Archive: "+archive.getFilePath());
			
			for(JarArchive clz : archive.dependsOnArchives()) {
				LOG.info(" - Depends On: "+clz.getFilePath());
			}

			for(JarArchive clz : archive.providesForArchives()) {
				LOG.info(" - Provides For: "+clz.getFilePath());
			}
		}
	}
}
