package org.jboss.windup.graph;

import java.io.File;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.jboss.windup.graph.model.meta.DatasourceMeta;
import org.jboss.windup.graph.model.meta.JBossModuleMeta;
import org.jboss.windup.graph.model.meta.JMSMeta;
import org.jboss.windup.graph.model.meta.JarManifest;
import org.jboss.windup.graph.model.meta.MailserverMeta;
import org.jboss.windup.graph.model.meta.SpecificationVersionMeta;
import org.jboss.windup.graph.model.meta.javaclass.EjbEntityFacet;
import org.jboss.windup.graph.model.meta.javaclass.EjbSessionBeanFacet;
import org.jboss.windup.graph.model.meta.javaclass.HibernateEntityFacet;
import org.jboss.windup.graph.model.meta.javaclass.MessageDrivenBeanFacet;
import org.jboss.windup.graph.model.meta.javaclass.SpringBeanFacet;
import org.jboss.windup.graph.model.meta.xml.DoctypeMeta;
import org.jboss.windup.graph.model.meta.xml.EjbConfigurationFacet;
import org.jboss.windup.graph.model.meta.xml.HibernateConfigurationFacet;
import org.jboss.windup.graph.model.meta.xml.MavenFacet;
import org.jboss.windup.graph.model.meta.xml.NamespaceMeta;
import org.jboss.windup.graph.model.meta.xml.SpringConfigurationFacet;
import org.jboss.windup.graph.model.resource.ArchiveEntryResource;
import org.jboss.windup.graph.model.resource.ArchiveResource;
import org.jboss.windup.graph.model.resource.EarArchive;
import org.jboss.windup.graph.model.resource.JarArchive;
import org.jboss.windup.graph.model.resource.JavaClass;
import org.jboss.windup.graph.model.resource.TempArchiveResource;
import org.jboss.windup.graph.model.resource.WarArchive;
import org.jboss.windup.graph.model.resource.XmlResource;

import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanKey;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerModule;
import com.tinkerpop.frames.modules.typedgraph.TypedGraphModuleBuilder;

public class GraphContextImpl implements GraphContext {

	private TitanGraph graph;
	private BatchGraph<TitanGraph> batch;
	private FramedGraph<TitanGraph> framed;
	
	public TitanGraph getGraph() {
		return graph;
	}
	
	public BatchGraph<TitanGraph> getBatch() {
		return batch;
	}
	
	public FramedGraph<TitanGraph> getFramed() {
		return framed;
	}
	
	public GraphContextImpl(File diskCache) {
		FileUtils.deleteQuietly(diskCache);
		
		File lucene = new File(diskCache, "graphsearch");
		File berkley = new File(diskCache, "graph");
		
		Configuration conf = new BaseConfiguration();
		conf.setProperty("storage.directory", berkley.getAbsolutePath());
		conf.setProperty("storage.backend", "berkeleyje");
		
		conf.setProperty("storage.index.search.backend", "lucene");
		conf.setProperty("storage.index.search.directory", lucene.getAbsolutePath());
		conf.setProperty("storage.index.search.client-only", "false");
		conf.setProperty("storage.index.search.local-mode", "true");
		
		final String INDEX_NAME = "search";
		
		graph = TitanFactory.open(conf);
		//graph.createKeyIndex("archiveEntry", Vertex.class);
		//graph.createKeyIndex("filePath", Vertex.class, new Parameter<String, String>("type", "UNIQUE"));
		//graph.createKeyIndex("type", Vertex.class);
		
		TitanKey namespaceURIKey = graph.makeKey("namespaceURI").dataType(String.class).
				indexed(Vertex.class).make();

		TitanKey schemaLocationKey = graph.makeKey("schemaLocation").dataType(String.class).
				indexed(Vertex.class).make();
		
		TitanKey publicIdKey = graph.makeKey("publicId").dataType(String.class).
				indexed(Vertex.class).make();
		
		TitanKey rootTagKey = graph.makeKey("rootTagName").dataType(String.class).
				indexed(Vertex.class).make();
		
		TitanKey systemIdKey = graph.makeKey("systemId").dataType(String.class).
				indexed(Vertex.class).make();
		
		TitanKey qualifiedNameKey = graph.makeKey("qualifiedName").dataType(String.class).
				indexed(Vertex.class).unique().make();
		
		TitanKey archiveEntryKey = graph.makeKey("archiveEntry").dataType(String.class).
				indexed("search", Vertex.class).make();
		
		TitanKey typeKey = graph.makeKey("type").dataType(String.class).
				indexed(Vertex.class).make();
		
		TitanKey filePath = graph.makeKey("filePath").dataType(String.class).
				indexed(Vertex.class).unique().make();
		
		TitanKey mavenIdentifier = graph.makeKey("mavenIdentifier").dataType(String.class).
				indexed(Vertex.class).unique().make();
		
		batch = new BatchGraph<TitanGraph>(graph, 1000L);
		
		FramedGraphFactory factory = new FramedGraphFactory(
				new JavaHandlerModule(),
			    new TypedGraphModuleBuilder()
			    .withClass(ArchiveResource.class)
			    .withClass(ArchiveEntryResource.class)
			    .withClass(EarArchive.class)
			    .withClass(org.jboss.windup.graph.model.resource.FileResource.class)
		        .withClass(JarArchive.class)
			    .withClass(JavaClass.class)
		        .withClass(org.jboss.windup.graph.model.resource.Resource.class)
			    .withClass(WarArchive.class)
			    .withClass(XmlResource.class)
			    .withClass(TempArchiveResource.class)
			    
			    .withClass(EjbEntityFacet.class)
			    .withClass(EjbSessionBeanFacet.class)
			    .withClass(HibernateEntityFacet.class)
			    .withClass(MessageDrivenBeanFacet.class)
			    .withClass(SpringBeanFacet.class)
			    
			    .withClass(MavenFacet.class)
			    .withClass(EjbConfigurationFacet.class)
			    .withClass(SpringConfigurationFacet.class)
			    .withClass(HibernateConfigurationFacet.class)
			    .withClass(NamespaceMeta.class)
			    .withClass(DoctypeMeta.class)
			    
			    .withClass(DatasourceMeta.class)
				.withClass(JBossModuleMeta.class)
				.withClass(JMSMeta.class)
				.withClass(MailserverMeta.class)
				.withClass(SpecificationVersionMeta.class)
			    .withClass(JarManifest.class)
			    
			    .build(), 
			    new GremlinGroovyModule()
		);
		
		framed = factory.create(graph);
	}
	
}
