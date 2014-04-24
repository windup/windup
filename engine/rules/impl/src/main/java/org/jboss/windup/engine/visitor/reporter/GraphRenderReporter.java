package org.jboss.windup.engine.visitor.reporter;

import java.io.File;
import java.io.FileOutputStream;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.WindupContext;
import org.jboss.windup.graph.renderer.GraphMLRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;

/**
 * Serializes the graph to GraphML.
 * 
 * @author bradsdavis@gmail.com
 * 
 */
public class GraphRenderReporter extends AbstractGraphVisitor
{
    private static final Logger LOG = LoggerFactory.getLogger(GraphRenderReporter.class);

    @Inject
    private GraphMLRenderer graphMLRenderer;

    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.REPORTING;
    }

    @Override
    public void run()
    {
        File graphLocation = new File("/home/jsightler/tmp/", "graphml.graphml");
        graphMLRenderer.renderGraphML(graphLocation);
        LOG.debug("Wrote graph to: " + graphLocation.getAbsolutePath());
    }

}
