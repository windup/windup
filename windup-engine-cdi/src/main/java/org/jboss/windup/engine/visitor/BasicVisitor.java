package org.jboss.windup.engine.visitor;

import java.io.File;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.jboss.windup.graph.dao.FileDaoBean;

public class BasicVisitor extends EmptyGraphVisitor {

	@Inject
	private FileDaoBean fileDao;
	
	@Override
	public void visit() {
		File reference = new File("/Users/bradsdavis/JBoss/jboss-eap-6.1/standalone/deployments/custom-application-remote.war");
		org.jboss.windup.graph.model.resource.File graphReference = fileDao.getByFilePath(reference.getAbsolutePath());
	}
}
