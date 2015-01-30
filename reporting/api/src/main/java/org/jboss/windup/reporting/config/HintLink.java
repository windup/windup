package org.jboss.windup.reporting.config;

import org.ocpsoft.rewrite.config.OperationBuilder;

/**
 * One of the builder interfaces of Hint operation.
 * @author mbriskar
 *
 */
public interface HintLink
{

    /**
     * Adds effort to the Hint. The effort represents the level of effort required to fix a task.
     * @param effort number of effort to be added to hint
     * @return 
     */
    OperationBuilder withEffort(int effort);
    
}
