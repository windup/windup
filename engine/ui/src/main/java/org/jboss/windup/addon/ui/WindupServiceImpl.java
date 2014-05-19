package org.jboss.windup.addon.ui;

import javax.inject.Inject;

import org.jboss.windup.addon.engine.WindupProcessor;
import org.jboss.windup.graph.GraphContext;

public class WindupServiceImpl implements WindupService
{
    @Inject
    private GraphContext graphContext;

    @Inject
    private WindupProcessor windupProcessor;

    @Override
    public WindupServiceConfigurationModel createServiceConfiguration()
    {
        return graphContext.getFramed().addVertex(null, WindupServiceConfigurationModel.class);
    }

    @Override
    public void execute(WindupServiceConfigurationModel cfg)
    {
        windupProcessor.execute();
    }
}
