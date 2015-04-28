package org.jboss.windup.rules.apps.java.decompiler;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.decompiler.util.Filter;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.model.PackageModel;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.jboss.windup.util.Logging;

/**
 * Filters classes in an archive based upon their package name.
 */
public class ZipEntryPackageFilter implements Filter<ZipEntry>
{
    private static final Logger log = Logging.get(ZipEntryPackageFilter.class);

    private WindupJavaConfigurationService configurationService;

    /**
     * Create the filter using the given {@link GraphContext}.
     */
    public ZipEntryPackageFilter(GraphContext context)
    {
        this.configurationService = new WindupJavaConfigurationService(context);
    }

    private Set<String> convertToStringFilters(Iterable<PackageModel> packages)
    {
        Set<String> result = new HashSet<>();
        for (PackageModel model : packages)
        {
            String packageName = model.getPackageName();
            String entryFilter = StringUtils.replace(packageName, ".", "/");
            result.add(entryFilter);
        }
        return result;
    }

    @Override
    public Result decide(ZipEntry entry)
    {
        return this.configurationService.shouldScanFile(entry.getName()) ? Result.ACCEPT : Result.REJECT;
    }

}
