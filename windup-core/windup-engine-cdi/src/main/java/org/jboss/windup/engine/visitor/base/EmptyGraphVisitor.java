package org.jboss.windup.engine.visitor.base;

import org.jboss.windup.graph.model.meta.javaclass.EjbEntityFacet;
import org.jboss.windup.graph.model.meta.javaclass.EjbServiceFacet;
import org.jboss.windup.graph.model.meta.javaclass.MessageDrivenBeanFacet;
import org.jboss.windup.graph.model.meta.javaclass.SpringBeanFacet;
import org.jboss.windup.graph.model.meta.xml.DoctypeMeta;
import org.jboss.windup.graph.model.meta.xml.EjbConfigurationFacet;
import org.jboss.windup.graph.model.meta.xml.SpringConfigurationFacet;
import org.jboss.windup.graph.model.resource.ArchiveEntryResource;
import org.jboss.windup.graph.model.resource.ArchiveResource;
import org.jboss.windup.graph.model.resource.EarArchive;
import org.jboss.windup.graph.model.resource.FileResource;
import org.jboss.windup.graph.model.resource.JarArchive;
import org.jboss.windup.graph.model.resource.JavaClass;
import org.jboss.windup.graph.model.resource.Resource;
import org.jboss.windup.graph.model.resource.WarArchive;
import org.jboss.windup.graph.model.resource.XmlResource;

public abstract class EmptyGraphVisitor implements GraphVisitor {

	@Override
	public abstract void run();

	@Override
	public void visitResource(Resource entry) {
		//nothing.		
	}

	@Override
	public void visitFile(FileResource entry) {
		//nothing.		
	}

	@Override
	public void visitArchive(ArchiveResource entry) {
		//nothing.		
	}

	@Override
	public void visitArchiveEntry(ArchiveEntryResource entry) {
		//nothing.		
	}

	@Override
	public void visitEarArchive(EarArchive entry) {
		//nothing.		
	}

	@Override
	public void visitJarArchive(JarArchive entry) {
		//nothing.		
	}

	@Override
	public void visitWarArchive(WarArchive entry) {
		//nothing.		
	}

	@Override
	public void visitJavaClass(JavaClass entry) {
		//nothing.		
	}

	@Override
	public void visitEjbEntity(EjbEntityFacet entry) {
		//nothing.		
	}

	@Override
	public void visitEjbService(EjbServiceFacet entry) {
		//nothing.		
	}

	@Override
	public void visitMessageDrivenBean(MessageDrivenBeanFacet entry) {
		//nothing.		
	}

	@Override
	public void visitEjbEntity(SpringBeanFacet entry) {
		//nothing.		
	}

	@Override
	public void visitXmlResource(XmlResource entry) {
		//nothing.		
	}

	@Override
	public void visitEjbConfiguration(EjbConfigurationFacet entry) {
		//nothing.		
	}

	@Override
	public void visitSpringConfiguration(SpringConfigurationFacet entry) {
		//nothing.		
	}
	
	@Override
	public void visitDoctype(DoctypeMeta entry) {
		//nothing.
	}

}
