package org.jboss.windup.graph.renderer;

import java.io.File;

/**
 * Renders the graph to a RDF file
 * 
 * @author jsightler
 *
 */
public interface GraphRDFRenderer
{
    public void renderRDF(File outputFile);
}
