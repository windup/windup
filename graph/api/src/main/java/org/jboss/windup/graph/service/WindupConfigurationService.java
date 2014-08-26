package org.jboss.windup.graph.service;

import javax.inject.Inject;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.PackageModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;

/**
 * Helper methods for accessing the WindupConfigurationModel and associated data.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class WindupConfigurationService extends GraphService<WindupConfigurationModel>
{

    public WindupConfigurationService()
    {
        super(WindupConfigurationModel.class);
    }

    @Inject
    public WindupConfigurationService(GraphContext context)
    {
        super(context, WindupConfigurationModel.class);
    }

    /**
     * Indicates whether the provided package should be scanned (based upon the inclusion/exclusion lists).
     */
    public boolean shouldScanPackage(String pkg)
    {
        WindupConfigurationModel cfg = getConfigurationModel(getGraphContext());
        for (PackageModel pkgModel : cfg.getExcludeJavaPackages())
        {
            String excludePkg = pkgModel.getPackageName();
            if (pkg.startsWith(excludePkg))
            {
                return false;
            }
        }

        for (PackageModel pkgModel : cfg.getScanJavaPackages())
        {
            String includePkg = pkgModel.getPackageName();
            if (pkg.startsWith(includePkg))
            {
                return true;
            }
        }

        return false;
    }
}
