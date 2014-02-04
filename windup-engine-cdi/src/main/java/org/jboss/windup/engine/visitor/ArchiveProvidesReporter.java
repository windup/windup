package org.jboss.windup.engine.visitor;

import javax.inject.Inject;

import org.jboss.windup.engine.WindupContext;
import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.jboss.windup.graph.dao.JarArchiveDao;
import org.jboss.windup.graph.model.resource.JarArchive;
import org.jboss.windup.graph.model.resource.JavaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArchiveProvidesReporter extends EmptyGraphVisitor {

	private static final Logger LOG = LoggerFactory.getLogger(ArchiveProvidesReporter.class);
	
	@Inject
	private JarArchiveDao jarDao;
	
	@Override
	public void visitContext(WindupContext context) {
		for(JarArchive archive : jarDao.getAll()) {
			LOG.info("Archive: "+archive.getFilePath());
			
			for(JavaClass clz : archive.getJavaClasses()) {
				LOG.info(" - Provides: "+clz.getQualifiedName());
			}
		}
	}
}
