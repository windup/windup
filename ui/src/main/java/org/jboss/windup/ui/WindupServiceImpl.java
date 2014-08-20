package org.jboss.windup.ui;

import java.nio.file.Path;

import javax.inject.Inject;

import org.jboss.windup.engine.WindupProcessor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.GraphService;

public class WindupServiceImpl implements WindupService
{
    @Inject
    private GraphContext graphContext;

    @Inject
    private WindupProcessor windupProcessor;

    @Override
    public WindupConfigurationModel createServiceConfiguration(Path outputFolder)
    {
        windupProcessor.setOutputDirectory(outputFolder);
        return GraphService.getConfigurationModel(graphContext);
    }

    @Override
    public void execute()
    {
        try
        {
            windupProcessor.execute();
        }
        finally
        {
            graphContext.disconnectFromGraph();
        }
    }
}
