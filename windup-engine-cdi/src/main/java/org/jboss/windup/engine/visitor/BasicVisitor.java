package org.jboss.windup.engine.visitor;

import java.io.File;

import javax.inject.Inject;

import org.jboss.windup.engine.WindupContext;
import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.jboss.windup.graph.dao.FileDao;

public class BasicVisitor extends EmptyGraphVisitor {

	@Inject
	private FileDao fileDao;
	
	@Override
	public void visitContext(WindupContext context) {
		File reference = new File("/Users/bradsdavis/JBoss/jboss-eap-6.1/standalone/deployments/custom-application-remote.war");
		org.jboss.windup.graph.model.resource.File graphReference = fileDao.getByFilePath(reference.getAbsolutePath());
	}
}
