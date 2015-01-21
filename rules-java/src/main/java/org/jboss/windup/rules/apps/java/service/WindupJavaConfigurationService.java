package org.jboss.windup.rules.apps.java.service;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.report.IgnoredFileRegexModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.PackageModel;
import org.jboss.windup.rules.apps.java.model.WindupJavaConfigurationModel;

/**
 * Provides methods for loading and working with {@link WindupJavaConfigurationModel} objects.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class WindupJavaConfigurationService extends GraphService<WindupJavaConfigurationModel>
{

    private List<String> ignoredRegexes;

    public WindupJavaConfigurationService(GraphContext context)
    {
        super(context, WindupJavaConfigurationModel.class);
    }

    /**
     * Loads the single {@link WindupJavaConfigurationModel} from the graph.
     */
    public static synchronized WindupJavaConfigurationModel getJavaConfigurationModel(GraphContext context)
    {
        WindupJavaConfigurationService service = new WindupJavaConfigurationService(context);
        WindupJavaConfigurationModel config = service.getUnique();
        if (config == null)
            config = service.create();
        return config;
    }

    public List<String> getIgnoredFileRegexes()
    {
        if (ignoredRegexes == null)
        {
            ignoredRegexes = new ArrayList<String>();

            WindupJavaConfigurationModel cfg = getJavaConfigurationModel(getGraphContext());
            for (IgnoredFileRegexModel ignored : cfg.getIgnoredFileRegexes())
            {
                //TODO: Consider having isCompilable() in case there is no message but is not compilable
            	if(ignored.getCompilationError() == null) {
            		ignoredRegexes.add(ignored.getRegex());
            	}
            }
        }
        return ignoredRegexes;
    }

    /**
     * Indicates whether the provided package should be scanned (based upon the inclusion/exclusion lists).
     */
    public boolean shouldScanPackage(String pkg)
    {
        // assume an empty string if it wasn't specified
        if (pkg == null)
        {
            pkg = "";
        }
        WindupJavaConfigurationModel cfg = getJavaConfigurationModel(getGraphContext());
        for (PackageModel pkgModel : cfg.getExcludeJavaPackages())
        {
            String excludePkg = pkgModel.getPackageName();
            if (pkg.startsWith(excludePkg))
            {
                return false;
            }
        }

        // if the list is empty, assume it is intended to just accept all packages
        if (!cfg.getScanJavaPackages().iterator().hasNext())
        {
            return true;
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
