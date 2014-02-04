package org.jboss.windup.engine.visitor;

import javax.inject.Inject;

import org.jboss.windup.engine.WindupContext;
import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.jboss.windup.graph.dao.JavaClassDao;
import org.jboss.windup.graph.model.resource.JavaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassNotFoundReporter extends EmptyGraphVisitor {

	private static final Logger LOG = LoggerFactory.getLogger(ClassNotFoundReporter.class);
	
	@Inject
	private JavaClassDao javaClassDao;
	
	@Override
	public void visitContext(WindupContext context) {
		for(JavaClass clz : javaClassDao.getAllClassNotFound()) {
			LOG.info("Class not found: "+clz.getQualifiedName());
		}
	}
}
