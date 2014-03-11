package org.jboss.windup.engine.visitor;

import java.io.File;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.base.AbstractGraphVisitor;
import org.jboss.windup.graph.dao.FileResourceDaoBean;

public class BasicVisitor extends AbstractGraphVisitor {

	@Inject
	private FileResourceDaoBean fileDao;
	
	@Override
	public void run() {
		File r1 = new File("/Users/bradsdavis/JBoss/jboss-eap-6.1/standalone/deployments/custom-application-remote.war");
		org.jboss.windup.graph.model.resource.FileResource r1g = fileDao.getByFilePath(r1.getAbsolutePath());
		
		File r2 = new File("/Users/bradsdavis/Projects/migrations/inputs/WindupConfigurations.jar");
		org.jboss.windup.graph.model.resource.FileResource r2g = fileDao.getByFilePath(r2.getAbsolutePath());
	}
}
