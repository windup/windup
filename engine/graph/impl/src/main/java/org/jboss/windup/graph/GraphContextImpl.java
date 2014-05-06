package org.jboss.windup.graph;

import java.io.File;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.jboss.windup.graph.typedgraph.GraphTypeRegistry;

import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanKey;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.batch.BatchGraph;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerModule;

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
	
	public GraphContextImpl(File diskCache, GraphTypeRegistry graphTypeRegistry) {
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
			    graphTypeRegistry.build(), 
			    new GremlinGroovyModule()
		);
		
		framed = factory.create(graph);
	}
	
}
