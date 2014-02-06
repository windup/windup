package org.jboss.windup.engine.visitor.base;

import org.jboss.windup.graph.model.meta.javaclass.EjbEntityFacet;
import org.jboss.windup.graph.model.meta.javaclass.EjbServiceFacet;
import org.jboss.windup.graph.model.meta.javaclass.MessageDrivenBeanFacet;
import org.jboss.windup.graph.model.meta.javaclass.SpringBeanFacet;
import org.jboss.windup.graph.model.meta.xml.EjbConfigurationFacet;
import org.jboss.windup.graph.model.meta.xml.SpringConfigurationFacet;
import org.jboss.windup.graph.model.resource.Archive;
import org.jboss.windup.graph.model.resource.ArchiveEntryResource;
import org.jboss.windup.graph.model.resource.EarArchive;
import org.jboss.windup.graph.model.resource.File;
import org.jboss.windup.graph.model.resource.JarArchive;
import org.jboss.windup.graph.model.resource.JavaClass;
import org.jboss.windup.graph.model.resource.Resource;
import org.jboss.windup.graph.model.resource.WarArchive;
import org.jboss.windup.graph.model.resource.XmlResource;

public interface GraphVisitor {

	public void visit();
	public void visitResource(Resource entry);
	public void visitFile(File entry);
	
	public void visitArchive(Archive entry);
	public void visitArchiveEntry(ArchiveEntryResource entry);
	public void visitEarArchive(EarArchive entry);
	public void visitJarArchive(JarArchive entry);
	public void visitWarArchive(WarArchive entry);
	
	public void visitJavaClass(JavaClass entry);
	public void visitEjbEntity(EjbEntityFacet entry);
	public void visitEjbService(EjbServiceFacet entry);
	public void visitMessageDrivenBean(MessageDrivenBeanFacet entry);
	public void visitEjbEntity(SpringBeanFacet entry);
	
	public void visitXmlResource(XmlResource entry);
	public void visitEjbConfiguration(EjbConfigurationFacet entry);
	public void visitSpringConfiguration(SpringConfigurationFacet entry);
}
