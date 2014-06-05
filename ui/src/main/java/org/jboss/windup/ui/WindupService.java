package org.jboss.windup.ui;

import org.jboss.windup.graph.model.WindupConfigurationModel;

public interface WindupService
{

    public WindupConfigurationModel createServiceConfiguration();

    public void execute(WindupConfigurationModel cfg);

}
