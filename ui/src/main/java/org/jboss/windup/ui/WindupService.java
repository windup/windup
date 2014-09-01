package org.jboss.windup.ui;

import org.jboss.windup.engine.WindupProcessorConfig;
import org.jboss.windup.engine.WindupProgressMonitor;
import org.jboss.windup.graph.model.WindupConfigurationModel;

/**
 * Provides a service for configuring an running an instance of Windup.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public interface WindupService
{

    /**
     * Creates the WindupConfiguration in the Graph, also connects the graph at the location specified by
     * "outputDirectory".
     */
    public WindupConfigurationModel createServiceConfiguration();

    /**
     * Execute Windup
     */
    public void execute();

    /**
     * Execute Windup using the given {@link WindupProgressMonitor} to receive updates on progress.
     */
    public void execute(WindupProcessorConfig config);

}
