package org.jboss.windup.engine.visitor.reporter;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.jboss.windup.addon.config.RulePhase;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.WindupContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;

/**
 * Writes Windup graph to GraphML file.
 * 
 * @author bradsdavis@gmail.com
 * 
 */
public class WriteGraphToGraphMLReporter extends AbstractGraphVisitor
{

    private static final Logger LOG = LoggerFactory.getLogger(WriteGraphToGraphMLReporter.class);

    @Inject
    private WindupContext windupContext;
    @Inject
    private GraphContext graphContext;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORTING;
    }

    @Override
    public void run()
    {
        GraphMLWriter writer = new GraphMLWriter(graphContext.getGraph());
        File graphFile = new File(windupContext.getRunDirectory(), "graphml.graphml");
        try
        {
            writer.outputGraph(graphFile.getAbsolutePath());
            LOG.info("Wrote graph to: " + graphFile.getAbsolutePath());
        }
        catch (IOException e)
        {
            throw new RuntimeException("Exception writing graph to: " + graphFile.getAbsolutePath(), e);
        }
    }
}
