package org.jboss.windup.reporting.renderer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerModule;
import com.tinkerpop.frames.modules.typedgraph.TypeField;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import com.tinkerpop.frames.modules.typedgraph.TypedGraphModuleBuilder;

public class SimpleGraphRenderer {

	private Graph graph;
	private FramedGraph<Graph> framed;
	private final GraphExporter exporter;
	private final String edgeLabel;
	
	public Graph getGraph() {
		return graph;
	}
	
	public FramedGraph<Graph> getFramed() {
		return framed;
	}
	
	public SimpleGraphRenderer(String edgeLabel) {
		this.edgeLabel = edgeLabel;
		
		graph = new TinkerGraph();
		exporter = new GraphExporter(graph);
		
		FramedGraphFactory factory = new FramedGraphFactory(
				new JavaHandlerModule(),
			    new TypedGraphModuleBuilder()
				.withClass(RenderableVertex.class)
			    .build(), 
			    new GremlinGroovyModule()
		);
		
		framed = factory.create(graph);
	}
	
	@TypeValue("RenderableVertex")
	public static interface RenderableVertex extends WindupVertexFrame {
		
		@Property("label")
		public void setLabel(String label);
		
		@Property("label")
		public String getLabel(String label);

		@Adjacency(label = "in", direction = Direction.IN)
		public Iterable<RenderableVertex> getIns();

		@Adjacency(label = "in", direction = Direction.IN)
		public void addIn(RenderableVertex parent);
		
		@Adjacency(label = "out", direction = Direction.OUT)
		public Iterable<RenderableVertex> getOuts();

		@Adjacency(label = "out", direction = Direction.OUT)
		public void addOut(RenderableVertex out);
	}
	
	public void renderVizjs(File output) throws RuntimeException {
		try {
			exporter.renderVizjs(output, "label", edgeLabel);
		}
		catch(Exception e) {
			throw new RuntimeException("Exception writing graph.", e);
		}
	}
	
	public void renderSigma(File output) throws RuntimeException {
		try {
			exporter.renderSigma(output, "label", edgeLabel);
		}
		catch(Exception e) {
			throw new RuntimeException("Exception writing graph.", e);
		}
	}
	
	public void renderDagreD3(File output) throws RuntimeException {
		try {
			exporter.renderDagreD3(output, "label", edgeLabel);
		}
		catch(Exception e) {
			throw new RuntimeException("Exception writing graph.", e);
		}
		
	}
}
