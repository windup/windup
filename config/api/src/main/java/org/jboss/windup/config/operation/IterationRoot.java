package org.jboss.windup.config.operation;

import org.jboss.windup.config.operation.iteration.IterationPayloadManager;
import org.jboss.windup.config.operation.iteration.IterationSelectionManager;

/**
 * Commons for For and Iteration.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public interface IterationRoot {
    
    public void setPayloadManager(IterationPayloadManager payloadManager);
    
    public IterationPayloadManager getPayloadManager();
    
    
    public void setSelectionManager( IterationSelectionManager mgr );

    public IterationSelectionManager getSelectionManager();
}
