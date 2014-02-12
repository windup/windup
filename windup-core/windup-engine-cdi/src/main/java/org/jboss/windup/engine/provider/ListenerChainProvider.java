package org.jboss.windup.engine.provider;

import java.util.LinkedList;
import java.util.List;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.windup.engine.WindupContext;
import org.jboss.windup.engine.qualifier.ListenerChainQualifier;
import org.jboss.windup.engine.visitor.ArchiveEntryIndexVisitor;
import org.jboss.windup.engine.visitor.ArchiveHashVisitor;
import org.jboss.windup.engine.visitor.ArchiveTypingVisitor;
import org.jboss.windup.engine.visitor.BasicVisitor;
import org.jboss.windup.engine.visitor.DebugVisitor;
import org.jboss.windup.engine.visitor.EjbConfigurationVisitor;
import org.jboss.windup.engine.visitor.HibernateConfigurationVisitor;
import org.jboss.windup.engine.visitor.HibernateMappingVisitor;
import org.jboss.windup.engine.visitor.JavaClassVisitor;
import org.jboss.windup.engine.visitor.ManifestVisitor;
import org.jboss.windup.engine.visitor.MavenFacetVisitor;
import org.jboss.windup.engine.visitor.SpringConfigurationVisitor;
import org.jboss.windup.engine.visitor.XmlResourceVisitor;
import org.jboss.windup.engine.visitor.ZipArchiveGraphVisitor;
import org.jboss.windup.engine.visitor.base.GraphVisitor;
import org.jboss.windup.engine.visitor.reporter.ArchiveDependsOnReporter;
import org.jboss.windup.engine.visitor.reporter.ArchiveProvidesReporter;
import org.jboss.windup.engine.visitor.reporter.ClassNotFoundReporter;
import org.jboss.windup.engine.visitor.reporter.DuplicateClassReporter;
import org.jboss.windup.engine.visitor.reporter.EjbConfigurationReporter;
import org.jboss.windup.engine.visitor.reporter.GraphRenderReporter;
import org.jboss.windup.engine.visitor.reporter.HibernateConfigurationReporter;
import org.jboss.windup.engine.visitor.reporter.HibernateEntityReporter;
import org.jboss.windup.engine.visitor.reporter.JarManifestReporter;
import org.jboss.windup.engine.visitor.reporter.MavenPomReporter;
import org.jboss.windup.engine.visitor.reporter.NamespacesFoundReporter;
import org.jboss.windup.engine.visitor.reporter.WriteGraphToGraphMLReporter;
import org.jboss.windup.graph.model.meta.javaclass.HibernateEntityFacet;
import org.jboss.windup.graph.model.meta.xml.DoctypeMeta;
import org.jboss.windup.graph.model.meta.xml.HibernateConfigurationFacet;
import org.jboss.windup.graph.model.meta.xml.NamespaceMeta;

public class ListenerChainProvider {

	@Inject
	private WindupContext context;
	
	@Inject
	private BasicVisitor basic;
	
	@Inject
	private ZipArchiveGraphVisitor zipArchive;
	
	@Inject
	private ArchiveTypingVisitor archiveTypeVisitor;
	
	@Inject
	private ArchiveEntryIndexVisitor archiveEntryIndexingVisitor;
	
	@Inject
	private JavaClassVisitor javaClassVisitor;
	
	@Inject
	private XmlResourceVisitor xmlResourceVisitor;

	@Inject
	private MavenFacetVisitor mavenFacetVisitor;
	
	@Inject
	private SpringConfigurationVisitor springConfigurationVisitor;
	
	@Inject
	private ClassNotFoundReporter classNotFoundReporter;
	
	@Inject
	private DuplicateClassReporter duplicateClassReporter;
	
	@Inject
	private ArchiveProvidesReporter archiveProvidesReporter;
	
	@Inject
	private NamespacesFoundReporter namespacesFoundReporter;
	
	@Inject
	private GraphRenderReporter graphRenderReporter;
	
	@Inject
	private ArchiveDependsOnReporter archiveDependsOnReport;
	
	@Inject
	private HibernateConfigurationVisitor hibernateConfigurationVisitor;
	
	@Inject
	private HibernateMappingVisitor hibernateMappingVisitor;
	
	@Inject
	private EjbConfigurationVisitor ejbConfigurationVisitor;
	
	
	@Inject
	private ArchiveHashVisitor archiveHashVisitor;
	
	@Inject
	private WriteGraphToGraphMLReporter exportToMLreporter;
	
	@Inject
	private MavenPomReporter mavenPomReporter;
	
	@Inject
	private ManifestVisitor manifestVisitor;
	
	@Inject
	private JarManifestReporter manifestReporter;
	
	@Inject
	private HibernateConfigurationReporter hibernateConfigurationReporter;
	
	@Inject
	private HibernateEntityReporter hibernateEntityReporter;
	
	@Inject
	private EjbConfigurationReporter ejbConfigurationReporter;
	
	@ListenerChainQualifier
	@Produces
	public List<GraphVisitor> produceListenerChain() {
		List<GraphVisitor> listenerChain = new LinkedList<GraphVisitor>();
		listenerChain.add(basic);
		listenerChain.add(zipArchive); //recurses zip entries to expand
		listenerChain.add(archiveEntryIndexingVisitor); //indexes all entries to the graph
		listenerChain.add(archiveHashVisitor);
		listenerChain.add(archiveTypeVisitor);  //sets the archive to a sub-type
		listenerChain.add(manifestVisitor); //extracts manifest data.
		
		//listenerChain.add(javaClassVisitor); //loads java class information (imports / extends) to the graph
		listenerChain.add(xmlResourceVisitor); //loads xml resource information to the graph
		
		
		listenerChain.add(ejbConfigurationVisitor);
		listenerChain.add(ejbConfigurationReporter);
		
		//listenerChain.add(hibernateConfigurationVisitor); //loads hibernate configurations and processes
		//listenerChain.add(hibernateConfigurationReporter); //reports on hibernate configurations found
		
		//listenerChain.add(hibernateMappingVisitor); //loads hibernate entity mappings and processes
		//listenerChain.add(hibernateEntityReporter);
		
		
		
		//listenerChain.add(manifestReporter); //reports on hibernate configurations found
		
	//	listenerChain.add(new DebugVisitor(context, NamespaceMeta.class)); //extract Maven information to facet.
	//	listenerChain.add(new DebugVisitor(context, DoctypeMeta.class)); //extract Maven information to facet.
		
		
		//listenerChain.add(mavenFacetVisitor); //extract Maven information to facet.
		//listenerChain.add(springConfigurationVisitor);
		//listenerChain.add(new DebugVisitor(context, SpringConfigurationFacet.class)); //extract Maven information to facet.

		//listenerChain.add(archiveDependsOnReport);
		//listenerChain.add(exportToMLreporter);
		//listenerChain.add(mavenPomReporter);
		//listenerChain.add(duplicateClassReporter); //reports all classes found multiple times on the classpath.
		/*
		listenerChain.add(classNotFoundReporter); //reports all classes not found on the classpath.
		
		
		
		//listenerChain.add(graphRenderReporter);
		
		*/
		//listenerChain.add(namespacesFoundReporter);
		return listenerChain;
	}
}
