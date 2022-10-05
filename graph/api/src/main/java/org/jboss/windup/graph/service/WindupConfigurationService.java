package org.jboss.windup.graph.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Helper methods for accessing the WindupConfigurationModel and associated data.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class WindupConfigurationService extends GraphService<WindupConfigurationModel> {
    private static final String ARCHIVES = "archives";

    public WindupConfigurationService(GraphContext context) {
        super(context, WindupConfigurationModel.class);
    }

    public static Path getArchivesPath(final GraphContext graphContext) {
        WindupConfigurationModel cfg = WindupConfigurationService.getConfigurationModel(graphContext);
        String windupOutputFolder = cfg.getOutputPath().getFilePath();
        return Paths.get(windupOutputFolder, ARCHIVES);
    }

    /**
     * Return the global {@link WindupConfigurationModel} configuration for this execution of Windup.
     */
    public static synchronized WindupConfigurationModel getConfigurationModel(GraphContext context) {
        WindupConfigurationModel config = new GraphService<>(context, WindupConfigurationModel.class).getUnique();
        if (config == null)
            config = new GraphService<>(context, WindupConfigurationModel.class).create();
        return config;
    }

}
