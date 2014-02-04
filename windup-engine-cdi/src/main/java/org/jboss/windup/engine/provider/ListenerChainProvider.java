package org.jboss.windup.engine.provider;

import java.util.LinkedList;
import java.util.List;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.windup.engine.qualifier.ListenerChainQualifier;
import org.jboss.windup.engine.visitor.ArchiveDependsOnReporter;
import org.jboss.windup.engine.visitor.ArchiveEntryIndexVisitor;
import org.jboss.windup.engine.visitor.ArchiveProvidesReporter;
import org.jboss.windup.engine.visitor.ArchiveTypingVisitor;
import org.jboss.windup.engine.visitor.BasicVisitor;
import org.jboss.windup.engine.visitor.ClassNotFoundReporter;
import org.jboss.windup.engine.visitor.DebugVisitor;
import org.jboss.windup.engine.visitor.DuplicateClassReporter;
import org.jboss.windup.engine.visitor.GraphRenderReporter;
import org.jboss.windup.engine.visitor.JavaClassVisitor;
import org.jboss.windup.engine.visitor.NamespacesFoundReporter;
import org.jboss.windup.engine.visitor.XmlResourceVisitor;
import org.jboss.windup.engine.visitor.ZipArchiveGraphVisitor;
import org.jboss.windup.engine.visitor.base.GraphVisitor;
import org.jboss.windup.graph.model.resource.ArchiveEntryResource;
import org.jboss.windup.graph.model.resource.JavaClass;
import org.jboss.windup.graph.model.resource.XmlResource;

public class ListenerChainProvider {

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
	
	@ListenerChainQualifier
	@Produces
	public List<GraphVisitor> produceListenerChain() {
		List<GraphVisitor> listenerChain = new LinkedList<GraphVisitor>();
		listenerChain.add(basic);
		listenerChain.add(zipArchive); //recurses zip entries to expand
		listenerChain.add(archiveEntryIndexingVisitor); //indexes all entries to the graph
		listenerChain.add(archiveTypeVisitor);  //sets the archive to a sub-type
		listenerChain.add(javaClassVisitor); //loads java class information (imports / extends) to the graph
		listenerChain.add(xmlResourceVisitor); //loads xml resource information to the graph
		listenerChain.add(classNotFoundReporter); //reports all classes not found on the classpath.
		listenerChain.add(duplicateClassReporter); //reports all classes found multiple times on the classpath.
		listenerChain.add(namespacesFoundReporter);
		listenerChain.add(archiveProvidesReporter);
		listenerChain.add(graphRenderReporter);
		listenerChain.add(archiveDependsOnReport);
		
		return listenerChain;
	}
}
