package org.jboss.windup.graph;

import java.io.File;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.jboss.windup.graph.model.meta.JBossModuleMeta;
import org.jboss.windup.graph.model.meta.Meta;
import org.jboss.windup.graph.model.resource.facet.JavaClassFacet;
import org.jboss.windup.graph.model.resource.facet.XmlFacet;

import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Parameter;
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
		File lucene = new File(diskCache, "graphsearch");
		FileUtils.deleteQuietly(lucene);
		
		File berkley = new File(diskCache, "graph");
		FileUtils.deleteQuietly(berkley);
		
		Configuration conf = new BaseConfiguration();
		conf.setProperty("storage.directory", berkley.getAbsolutePath());
		conf.setProperty("storage.backend", "berkeleyje");
		conf.setProperty("storage.index.search.backend", "lucene");
		conf.setProperty("storage.index.search.directory", lucene.getAbsolutePath());

		graph = TitanFactory.open(conf);
		graph.createKeyIndex("qualifiedName", Vertex.class, new Parameter<String, String>("type", "UNIQUE"));
		
		batch = new BatchGraph<TitanGraph>(graph, 1000L);

		FramedGraphFactory factory = new FramedGraphFactory(
			    new TypedGraphModuleBuilder()
			    .withClass(JavaClassFacet.class)
		        .withClass(org.jboss.windup.graph.model.resource.File.class)
			    .withClass(XmlFacet.class)
			    .withClass(Meta.class)
			    .withClass(JBossModuleMeta.class)
			    .build()
		);
		
		framed = factory.create(graph);
	}
	
}
