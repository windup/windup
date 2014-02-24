package org.jboss.windup.engine.visitor;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.jboss.windup.graph.dao.JNDIReferenceDaoBean;
import org.jboss.windup.graph.dao.XmlResourceDaoBean;
import org.jboss.windup.graph.model.resource.XmlResource;

public class JNDIExtractorVisitor extends EmptyGraphVisitor {

	@Inject
	private XmlResourceDaoBean xmlDao;
	
	@Inject
	private JNDIReferenceDaoBean jndiDao;
	
	@Override
	public void run() {

		
		//for all JBoss web configs...
		for(XmlResource resource : xmlDao.findByRootTag("jboss-web")) {
			
		}
		
		//process all weblogic web configs..
		for(XmlResource resource : xmlDao.findByRootTag("weblogic-web-app")) {
			
		}
		
		//for all weblogic ejb configs...
		for(XmlResource resource : xmlDao.findByRootTag("weblogic-ejb-jar")) {
			
		}

		
		//for all oracle app servers web config...
		for(XmlResource resource : xmlDao.findByRootTag("orion-web-app")) {
			
		}

		//for all oracle app servers ejb config...
		for(XmlResource resource : xmlDao.findByRootTag("orion-ejb-jar")) {
			
		}

		
		//for all IBM Websphere webapp bindings...
		for(XmlResource resource : xmlDao.findByRootTag("WebAppBinding")) {
			
		}
		
		
		//for all IBM Websphere ejb bindings...
		for(XmlResource resource : xmlDao.findByRootTag("EJBJarBinding")) {
			
		}
	}

	
	
}
