package org.jboss.windup.graph;

import java.io.File;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.jboss.windup.graph.model.meta.JBossModuleMeta;
import org.jboss.windup.graph.model.meta.javaclass.EjbEntityFacet;
import org.jboss.windup.graph.model.meta.javaclass.MessageDrivenBeanFacet;
import org.jboss.windup.graph.model.meta.javaclass.SpringBeanFacet;
import org.jboss.windup.graph.model.meta.xml.EjbConfigurationFacet;
import org.jboss.windup.graph.model.meta.xml.SpringConfigurationFacet;
import org.jboss.windup.graph.model.resource.Archive;
import org.jboss.windup.graph.model.resource.ArchiveEntryResource;
import org.jboss.windup.graph.model.resource.EarArchive;
import org.jboss.windup.graph.model.resource.JarArchive;
import org.jboss.windup.graph.model.resource.JavaClass;
import org.jboss.windup.graph.model.resource.WarArchive;
import org.jboss.windup.graph.model.resource.XmlFile;

import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanKey;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.modules.typedgraph.TypedGraphModuleBuilder;

public class GraphContext {

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
	
	public GraphContext(File diskCache) {
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
		
		TitanKey qualifiedNameKey = graph.makeKey("qualifiedName").dataType(String.class).
				indexed(Vertex.class).unique().make();
		
		TitanKey archiveEntryKey = graph.makeKey("archiveEntry").dataType(String.class).
				indexed("search", Vertex.class).make();
		
		TitanKey typeKey = graph.makeKey("type").dataType(String.class).
				indexed(Vertex.class).make();
		
		TitanKey filePath = graph.makeKey("filePath").dataType(String.class).
				indexed(Vertex.class).unique().make();
			
		
		
		batch = new BatchGraph<TitanGraph>(graph, 1000L);
		
		FramedGraphFactory factory = new FramedGraphFactory(
			    new TypedGraphModuleBuilder()
			    .withClass(Archive.class)
			    .withClass(ArchiveEntryResource.class)
			    .withClass(EarArchive.class)
			    .withClass(org.jboss.windup.graph.model.resource.File.class)
		        .withClass(JarArchive.class)
			    .withClass(JavaClass.class)
		        .withClass(org.jboss.windup.graph.model.resource.Resource.class)
			    .withClass(WarArchive.class)
			    .withClass(XmlFile.class)
			    
			    .withClass(EjbEntityFacet.class)
			    .withClass(MessageDrivenBeanFacet.class)
			    .withClass(SpringBeanFacet.class)
			    
			    .withClass(EjbConfigurationFacet.class)
			    .withClass(SpringConfigurationFacet.class)
			    
			    .build()
		);
		
		framed = factory.create(graph);
	}
	
}
