package org.jboss.windup.engine.visitor.reporter;

import java.io.File;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jboss.windup.graph.renderer.GraphRDFRenderer;

public class RDFReporter extends AbstractGraphVisitor
{
    @Inject
    private GraphRDFRenderer graphRdfRenderer;
    
    @Override
    public VisitorPhase getPhase()
    {
        return VisitorPhase.REPORTING;
    }

    @Override
    public void run()
    {
        File graphLocation = new File("/tmp/", "rdfreport.rdf");
        graphRdfRenderer.renderRDF(graphLocation);
    }
}
