package org.jboss.windup.engine.visitor;

import java.util.List;

import org.jboss.windup.graph.model.meta.JarManifest;
import org.jboss.windup.graph.model.meta.PropertiesMeta;
import org.jboss.windup.graph.model.meta.javaclass.EjbEntityFacet;
import org.jboss.windup.graph.model.meta.javaclass.EjbSessionBeanFacet;
import org.jboss.windup.graph.model.meta.javaclass.MessageDrivenBeanFacet;
import org.jboss.windup.graph.model.meta.javaclass.SpringBeanFacet;
import org.jboss.windup.graph.model.meta.xml.DoctypeMeta;
import org.jboss.windup.graph.model.meta.xml.EjbConfigurationFacet;
import org.jboss.windup.graph.model.meta.xml.NamespaceMeta;
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

public interface GraphVisitor {
    public VisitorPhase getPhase();
    public List<Class<? extends GraphVisitor>> getDependencies();
    
	public void run();
	public void visitResource(Resource entry);
	public void visitFile(FileResource entry);
	
	public void visitArchive(ArchiveResource entry);
	public void visitArchiveEntry(ArchiveEntryResource entry);
	public void visitEarArchive(EarArchive entry);
	public void visitJarArchive(JarArchive entry);
	public void visitWarArchive(WarArchive entry);
	
	public void visitJavaClass(JavaClass entry);
	public void visitEjbEntity(EjbEntityFacet entry);
	public void visitEjbService(EjbSessionBeanFacet entry);
	public void visitMessageDrivenBean(MessageDrivenBeanFacet entry);
	public void visitEjbEntity(SpringBeanFacet entry);
	
	public void visitXmlResource(XmlResource entry);
	public void visitEjbConfiguration(EjbConfigurationFacet entry);
	public void visitSpringConfiguration(SpringConfigurationFacet entry);
	public void visitDoctype(DoctypeMeta entry);
	public void visitNamespace(NamespaceMeta entry);
	
    public void visitManifest(JarManifest entry);
    public void visitProperties(PropertiesMeta entry);

}
