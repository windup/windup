package org.jboss.windup.engine.visitor.reporter;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.jboss.windup.engine.WindupContext;
import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;

/**
 * Writes Windup graph to GraphML file.
 * 
 * @author bradsdavis@gmail.com
 *
 */
public class WriteGraphToGraphMLReporter extends EmptyGraphVisitor {

	private static final Logger LOG = LoggerFactory.getLogger(WriteGraphToGraphMLReporter.class);
	
	@Inject
	private WindupContext context;
	
	@Override
	public void run() {
		GraphMLWriter writer = new GraphMLWriter(context.getGraphContext().getGraph());
		File graphFile = new File(context.getRunDirectory(), "graphml.graphml");
		try {
			writer.outputGraph(graphFile.getAbsolutePath());
			LOG.info("Wrote graph to: "+graphFile.getAbsolutePath());
		} catch (IOException e) {
			throw new RuntimeException("Exception writing graph to: "+graphFile.getAbsolutePath(), e);
		}
	}
}
