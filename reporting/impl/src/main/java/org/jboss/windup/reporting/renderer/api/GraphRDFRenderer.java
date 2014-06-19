package org.jboss.windup.reporting.renderer.api;

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
