package org.jboss.windup.graph;

import java.nio.file.Path;

/**
 * Configuration of {@link GraphContext}. Serves for initialization.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class GraphContextConfig {

    private Path graphDataDir;
    
    private boolean warnOnLazyInit = false;


    //<editor-fold defaultstate="collapsed" desc="get/set">
    
    /**
     * The directory to store graph data in.
     */
    public Path getGraphDataDir()
    {
        return graphDataDir;
    }
    
    /**
     * Sets the directory to store graph data in.
     */
    public GraphContextConfig setGraphDataDir(Path path)
    {
        this.graphDataDir = path;
        return this;
    }
    
    /**
     * Whether to log a WARNING when initializing the GraphContext lazily.
     */
    public boolean isWarnOnLazyInit()
    {
        return warnOnLazyInit;
    }


    /**
     * Enables/disables to log a WARNING when initializing the GraphContext lazily.
     */
    public GraphContextConfig setWarnOnLazyInit(boolean warnOnLazyInit)
    {
        this.warnOnLazyInit = warnOnLazyInit;
        return this;
    }
    
    
    //</editor-fold>

}// class
