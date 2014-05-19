package org.jboss.windup.addon.ui;

public interface WindupService
{

    public WindupServiceConfigurationModel createServiceConfiguration();

    public void execute(WindupServiceConfigurationModel cfg);

}
