package org.jboss.windup.ui;

import javax.inject.Inject;

import org.jboss.windup.engine.WindupProcessor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;

public class WindupServiceImpl implements WindupService
{
    @Inject
    private GraphContext graphContext;

    @Inject
    private WindupProcessor windupProcessor;

    @Override
    public WindupConfigurationModel createServiceConfiguration()
    {
        return graphContext.getFramed().addVertex(null, WindupConfigurationModel.class);
    }

    @Override
    public void execute(WindupConfigurationModel cfg)
    {
        windupProcessor.execute();
    }
}
