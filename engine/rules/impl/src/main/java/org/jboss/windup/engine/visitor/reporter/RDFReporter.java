package org.jboss.windup.engine.visitor.reporter;

import java.io.File;

import javax.inject.Inject;

import org.jboss.windup.addon.config.RulePhase;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.graph.renderer.GraphRDFRenderer;

public class RDFReporter extends AbstractGraphVisitor
{
    @Inject
    private GraphRDFRenderer graphRdfRenderer;
    
    @Override
    public RulePhase getPhase()
    {
        return RulePhase.REPORTING;
    }

    @Override
    public void run()
    {
        File graphLocation = new File("/tmp/", "rdfreport.rdf");
        graphRdfRenderer.renderRDF(graphLocation);
    }
}
