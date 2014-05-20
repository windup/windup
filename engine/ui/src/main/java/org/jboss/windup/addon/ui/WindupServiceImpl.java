package org.jboss.windup.addon.ui;

import javax.inject.Inject;

import org.jboss.windup.addon.engine.WindupProcessor;
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
