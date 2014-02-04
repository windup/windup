package org.jboss.windup.engine.visitor;

import java.io.File;
import java.io.FileOutputStream;

import javax.inject.Inject;

import org.jboss.windup.engine.WindupContext;
import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;

/**
 * Goes through an archive adding the archive entries to the graph.
 * 
 * @author bradsdavis
 *
 */
public class GraphRenderReporter extends EmptyGraphVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(GraphRenderReporter.class);

	@Inject
	private WindupContext context;
	
	@Override
	public void visit() {
		File graphLocation = new File(context.getRunDirectory(), "graphml.graphml");
		GraphMLWriter graphML = new GraphMLWriter(context.getGraphContext().getGraph());
		try {
			graphML.outputGraph(new FileOutputStream(graphLocation));
			LOG.info("Wrote graph to: "+graphLocation.getAbsolutePath());
		} catch (Exception e) {
			LOG.error("Exception writing graph: ", e);
		}
	}
	
}
