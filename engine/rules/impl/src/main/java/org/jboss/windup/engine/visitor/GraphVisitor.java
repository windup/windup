package org.jboss.windup.engine.visitor;

import java.util.List;

import org.jboss.windup.graph.model.meta.JarManifestModel;
import org.jboss.windup.graph.model.meta.PropertiesMetaModel;
import org.jboss.windup.graph.model.meta.javaclass.EjbEntityFacetModel;
import org.jboss.windup.graph.model.meta.javaclass.EjbSessionBeanFacetModel;
import org.jboss.windup.graph.model.meta.javaclass.MessageDrivenBeanFacetModel;
import org.jboss.windup.graph.model.meta.javaclass.SpringBeanFacetModel;
import org.jboss.windup.graph.model.meta.xml.DoctypeMetaModel;
import org.jboss.windup.graph.model.meta.xml.EjbConfigurationFacetModel;
import org.jboss.windup.graph.model.meta.xml.NamespaceMetaModel;
import org.jboss.windup.graph.model.meta.xml.SpringConfigurationFacetModel;
import org.jboss.windup.graph.model.resource.ArchiveEntryResourceModel;
import org.jboss.windup.graph.model.resource.ArchiveResourceModel;
import org.jboss.windup.graph.model.resource.EarArchiveModel;
import org.jboss.windup.graph.model.resource.FileResourceModel;
import org.jboss.windup.graph.model.resource.JarArchiveModel;
import org.jboss.windup.graph.model.resource.JavaClassModel;
import org.jboss.windup.graph.model.resource.ResourceModel;
import org.jboss.windup.graph.model.resource.WarArchiveModel;
import org.jboss.windup.graph.model.resource.XmlResourceModel;

public interface GraphVisitor {
    public VisitorPhase getPhase();
    public List<Class<? extends GraphVisitor>> getDependencies();
    
	public void run();
	public void visitResource(ResourceModel entry);
	public void visitFile(FileResourceModel entry);
	
	public void visitArchive(ArchiveResourceModel entry);
	public void visitArchiveEntry(ArchiveEntryResourceModel entry);
	public void visitEarArchive(EarArchiveModel entry);
	public void visitJarArchive(JarArchiveModel entry);
	public void visitWarArchive(WarArchiveModel entry);
	
	public void visitJavaClass(JavaClassModel entry);
	public void visitEjbEntity(EjbEntityFacetModel entry);
	public void visitEjbService(EjbSessionBeanFacetModel entry);
	public void visitMessageDrivenBean(MessageDrivenBeanFacetModel entry);
	public void visitEjbEntity(SpringBeanFacetModel entry);
	
	public void visitXmlResource(XmlResourceModel entry);
	public void visitEjbConfiguration(EjbConfigurationFacetModel entry);
	public void visitSpringConfiguration(SpringConfigurationFacetModel entry);
	public void visitDoctype(DoctypeMetaModel entry);
	public void visitNamespace(NamespaceMetaModel entry);
	
    public void visitManifest(JarManifestModel entry);
    public void visitProperties(PropertiesMetaModel entry);

}
