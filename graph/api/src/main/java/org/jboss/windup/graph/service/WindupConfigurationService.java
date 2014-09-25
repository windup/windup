package org.jboss.windup.graph.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;

/**
 * Helper methods for accessing the WindupConfigurationModel and associated data.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class WindupConfigurationService extends GraphService<WindupConfigurationModel>
{
    public WindupConfigurationService(GraphContext context)
    {
        super(context, WindupConfigurationModel.class);
    }

    /**
     * Return the global {@link WindupConfigurationModel} configuration for this execution of Windup.
     */
    public static synchronized WindupConfigurationModel getConfigurationModel(GraphContext context)
    {
        WindupConfigurationModel config = new GraphService<>(context, WindupConfigurationModel.class).getUnique();
        if (config == null)
            config = new GraphService<>(context, WindupConfigurationModel.class).create();
        return config;
    }

}
