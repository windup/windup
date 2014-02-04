package org.jboss.windup.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.windup.graph.renderer.GraphExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindupEngine {
	private static final Logger LOG = LoggerFactory.getLogger(WindupEngine.class);
	
    public static void main(String[] args) throws FileNotFoundException, IOException {
    	Weld weld = new Weld();
    	WeldContainer container = weld.initialize();
    	
    	WindupProcessor processor = container.instance().select(WindupProcessor.class).get();
    	WindupContext context = container.instance().select(WindupContext.class).get();
    	processor.execute();
    	
    	GraphExporter renders = new GraphExporter(context.getGraphContext().getGraph());
		File targetFolder = FileUtils.getTempDirectory();
		
		LOG.info("Report: "+targetFolder.getAbsolutePath());
		
		/*
		for(Vertex v : context.getGraphContext().getGraph().getVertices()) {
			LOG.info("Vertex: "+v);
			for(String key : v.getPropertyKeys()) {
				LOG.info("  - "+key+" : "+v.getProperty(key));
			}
			for(Edge e : v.getEdges(Direction.IN, "child")) {
				LOG.info("  - Parent: "+e);
			}
			for(Edge e : v.getEdges(Direction.OUT, "child")) {
				LOG.info("  - Child: "+e);
			}
		}*/
		
    	weld.shutdown();
    }
}