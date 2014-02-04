package org.jboss.windup.engine.visitor;

import javax.inject.Inject;

import org.jboss.windup.engine.WindupContext;
import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.jboss.windup.graph.dao.JavaClassDao;
import org.jboss.windup.graph.model.resource.ArchiveEntryResource;
import org.jboss.windup.graph.model.resource.JavaClass;
import org.jboss.windup.graph.model.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DuplicateClassReporter extends EmptyGraphVisitor {

	private static final Logger LOG = LoggerFactory.getLogger(DuplicateClassReporter.class);
	
	@Inject
	private JavaClassDao javaClassDao;
	
	@Override
	public void visitContext(WindupContext context) {
		for(JavaClass clz : javaClassDao.getAllDuplicateClasses()) {
			LOG.info("Duplicate class: "+clz.getQualifiedName());
			
			for(Resource resource : clz.getResources()) {
				if(resource instanceof ArchiveEntryResource) {
					ArchiveEntryResource ar = (ArchiveEntryResource)resource;
					LOG.info(" - Provided by: "+ar.getArchive().getFilePath()+" -> "+ar.getArchiveEntry());
				}
			}
		}
	}
}
