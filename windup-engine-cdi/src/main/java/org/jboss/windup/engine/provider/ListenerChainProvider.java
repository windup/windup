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
import org.jboss.windup.engine.visitor.JavaClassVisitor;
import org.jboss.windup.engine.visitor.MavenFacetVisitor;
import org.jboss.windup.engine.visitor.XmlResourceVisitor;
import org.jboss.windup.engine.visitor.ZipArchiveGraphVisitor;
import org.jboss.windup.engine.visitor.base.GraphVisitor;
import org.jboss.windup.engine.visitor.reporter.ArchiveDependsOnReporter;
import org.jboss.windup.engine.visitor.reporter.ArchiveProvidesReporter;
import org.jboss.windup.engine.visitor.reporter.ClassNotFoundReporter;
import org.jboss.windup.engine.visitor.reporter.DuplicateClassReporter;
import org.jboss.windup.engine.visitor.reporter.GraphRenderReporter;
import org.jboss.windup.engine.visitor.reporter.NamespacesFoundReporter;
import org.jboss.windup.graph.model.meta.xml.MavenFacet;

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
	private ArchiveHashVisitor archiveHashVisitor;
	
	@ListenerChainQualifier
	@Produces
	public List<GraphVisitor> produceListenerChain() {
		List<GraphVisitor> listenerChain = new LinkedList<GraphVisitor>();
		listenerChain.add(basic);
		listenerChain.add(zipArchive); //recurses zip entries to expand
		listenerChain.add(archiveEntryIndexingVisitor); //indexes all entries to the graph
		listenerChain.add(archiveHashVisitor);
		listenerChain.add(archiveTypeVisitor);  //sets the archive to a sub-type
		
		//listenerChain.add(javaClassVisitor); //loads java class information (imports / extends) to the graph
		listenerChain.add(xmlResourceVisitor); //loads xml resource information to the graph
		listenerChain.add(mavenFacetVisitor); //extract Maven information to facet.
		listenerChain.add(new DebugVisitor(context, MavenFacet.class)); //extract Maven information to facet.
		
		/*
		listenerChain.add(classNotFoundReporter); //reports all classes not found on the classpath.
		listenerChain.add(duplicateClassReporter); //reports all classes found multiple times on the classpath.
		listenerChain.add(namespacesFoundReporter);
		listenerChain.add(archiveProvidesReporter);
		//listenerChain.add(graphRenderReporter);
		listenerChain.add(archiveDependsOnReport);
		*/
		return listenerChain;
	}
}
