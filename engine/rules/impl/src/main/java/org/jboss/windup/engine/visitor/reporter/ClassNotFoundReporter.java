package org.jboss.windup.engine.visitor.reporter;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.graph.dao.JavaClassDao;
import org.jboss.windup.graph.model.resource.JavaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Display Java Classes not found but referenced by another class.
 *  
 * @author bradsdavis@gmail.com
 *
 */
public class ClassNotFoundReporter extends AbstractGraphVisitor {

	private static final Logger LOG = LoggerFactory.getLogger(ClassNotFoundReporter.class);
	
	@Inject
	private JavaClassDao javaClassDao;
	
	@Override
	public void run() {
		for(JavaClass clz : javaClassDao.getAllClassNotFound()) {
			LOG.info("Class not found: "+clz.getQualifiedName());
		}
	}
}
