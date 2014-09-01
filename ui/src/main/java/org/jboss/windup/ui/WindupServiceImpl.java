package org.jboss.windup.ui;

import javax.inject.Inject;
import org.jboss.windup.engine.WindupProcessor;
import org.jboss.windup.engine.WindupProcessorConfig;
import org.jboss.windup.engine.WindupProgressMonitor;
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
    public WindupConfigurationModel createServiceConfiguration()
    {
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

    /**
     * Execute with given ProgressMonitor.
     */
    @Override
    public void execute(WindupProcessorConfig config)
    {
        try
        {
            windupProcessor.execute(config);
        }
        finally
        {
            graphContext.disconnectFromGraph();
        }
    }
}
