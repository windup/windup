package org.jboss.windup.engine.visitor;

import java.io.File;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.jboss.windup.graph.dao.FileResourceDaoBean;

public class BasicVisitor extends EmptyGraphVisitor {

	@Inject
	private FileResourceDaoBean fileDao;
	
	@Override
	public void run() {
		File reference = new File("/Users/bradsdavis/JBoss/jboss-eap-6.1/standalone/deployments/custom-application-remote.war");
		org.jboss.windup.graph.model.resource.FileResource graphReference = fileDao.getByFilePath(reference.getAbsolutePath());
	}
}
