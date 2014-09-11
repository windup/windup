package org.jboss.windup.graph;

import java.nio.file.Path;

/**
 * Configuration of {@link GraphContext}. Serves for initialization.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class GraphContextConfig {

    private Path graphDataDir;
    
    private boolean throwOnLazyInit = false;


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
     * Whether to throw an exception when initializing the GraphContext lazily.
     */
    public boolean isThrowOnLazyInit()
    {
        return throwOnLazyInit;
    }


    /**
     * Enables/disables to log a WARNING when initializing the GraphContext lazily.
     */
    public GraphContextConfig setThrowOnLazyInit(boolean throwOnLazyInit)
    {
        this.throwOnLazyInit = throwOnLazyInit;
        return this;
    }
    
    
    //</editor-fold>

}// class
