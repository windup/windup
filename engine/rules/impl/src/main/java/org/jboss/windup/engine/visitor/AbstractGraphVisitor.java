package org.jboss.windup.engine.visitor;

import java.util.Arrays;
import java.util.Collections;
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

public abstract class AbstractGraphVisitor implements GraphVisitor
{

    @Override
    public abstract void run();

    @Override
    public List<Class<? extends GraphVisitor>> getDependencies()
    {
        return Collections.emptyList();
    }

    @Override
    public void visitResource(ResourceModel entry)
    {
        // nothing.
    }

    @Override
    public void visitFile(FileResourceModel entry)
    {
        // nothing.
    }

    @Override
    public void visitArchive(ArchiveResourceModel entry)
    {
        // nothing.
    }

    @Override
    public void visitArchiveEntry(ArchiveEntryResourceModel entry)
    {
        // nothing.
    }

    @Override
    public void visitEarArchive(EarArchiveModel entry)
    {
        // nothing.
    }

    @Override
    public void visitJarArchive(JarArchiveModel entry)
    {
        // nothing.
    }

    @Override
    public void visitWarArchive(WarArchiveModel entry)
    {
        // nothing.
    }

    @Override
    public void visitJavaClass(JavaClassModel entry)
    {
        // nothing.
    }

    @Override
    public void visitEjbEntity(EjbEntityFacetModel entry)
    {
        // nothing.
    }

    @Override
    public void visitEjbService(EjbSessionBeanFacetModel entry)
    {
        // nothing.
    }

    @Override
    public void visitMessageDrivenBean(MessageDrivenBeanFacetModel entry)
    {
        // nothing.
    }

    @Override
    public void visitEjbEntity(SpringBeanFacetModel entry)
    {
        // nothing.
    }

    @Override
    public void visitXmlResource(XmlResourceModel entry)
    {
        // nothing.
    }

    @Override
    public void visitEjbConfiguration(EjbConfigurationFacetModel entry)
    {
        // nothing.
    }

    @Override
    public void visitSpringConfiguration(SpringConfigurationFacetModel entry)
    {
        // nothing.
    }

    @Override
    public void visitDoctype(DoctypeMetaModel entry)
    {
        // nothing.
    }

    @Override
    public void visitManifest(JarManifestModel entry)
    {
        // nothing.
    }

    @Override
    public void visitNamespace(NamespaceMetaModel entry)
    {
        // nothing.
    }

    @Override
    public void visitProperties(PropertiesMetaModel entry)
    {
        // nothing.
    }

    @SafeVarargs
    protected final List<Class<? extends GraphVisitor>> generateDependencies(Class<? extends GraphVisitor>... deps)
    {
        return Arrays.asList(deps);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[" + getClass().getSimpleName() + ": [Phase: " + getPhase() + ", Dependencies: " + getDependencies()
                    + "]");
        return sb.toString();
    }
}
