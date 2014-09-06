package org.jboss.windup.graph;

import java.nio.file.Path;

/**
 * Configuration of {@link GraphContext}. Serves for initialization.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class GraphContextConfig {

    private Path graphDataDir;


    //<editor-fold defaultstate="collapsed" desc="get/set">
    
    public Path getGraphDataDir()
    {
        return graphDataDir;
    }
    
    
    public GraphContextConfig setGraphDataDir(Path path)
    {
        this.graphDataDir = path;
        return this;
    }
    
    //</editor-fold>
    

}// class
