package org.jboss.windup.rules.apps.java.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.report.IgnoredFileRegexModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.IgnoredFileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.PackageModel;
import org.jboss.windup.rules.apps.java.model.WindupJavaConfigurationModel;
import org.jboss.windup.util.Logging;

import static java.lang.String.*;
import static java.lang.String.format;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

/**
 * Provides methods for loading and working with {@link WindupJavaConfigurationModel} objects.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * 
 */
public class WindupJavaConfigurationService extends GraphService<WindupJavaConfigurationModel>
{
    private static final Logger LOG = Logging.get(WindupJavaConfigurationService.class);

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

    /**
     * Checks if the {@link FileModel#getFilePath()} + {@link FileModel#getFileName()} is ignored by any of the specified regular expressions.
     */
    public boolean checkIfIgnored(final GraphRewrite event, FileModel file)
    {
        List<String> patterns = getIgnoredFileRegexes();
        boolean ignored = false;
        if (patterns != null && !patterns.isEmpty())
        {
            for (String pattern : patterns)
            {
                if (file.getFilePath().matches(pattern))
                {
                    IgnoredFileModel ignoredFileModel = GraphService.addTypeToModel(event.getGraphContext(), file, IgnoredFileModel.class);
                    ignoredFileModel.setIgnoredRegex(pattern);
                    LOG.info("File/Directory placed in " + file.getFilePath() + " was ignored, because matched [" + pattern + "].");
                    ignored = true;
                    break;
                }
            }
        }

        return ignored;
    }

    public List<String> getIgnoredFileRegexes()
    {
        if (ignoredRegexes == null)
        {
            ignoredRegexes = new ArrayList<>();

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
     * This is similar to {@link WindupJavaConfigurationService#shouldScanPackage(String)}, except that it expects to be given a file path (for
     * example, "/path/to/file.class"). This will use a string.contains approach, as we cannot know for sure what type of path prefixes may exist
     * before the package name part of the path.
     *
     * Also, this can only work reliably for class files, though it will generally work with java files if they are in package appropriate folders.
     */
    public boolean shouldScanFile(String path)
    {
        WindupJavaConfigurationModel configuration = getJavaConfigurationModel(getGraphContext());
        path = FilenameUtils.separatorsToUnix(path);
        for (PackageModel excludePackage : configuration.getExcludeJavaPackages())
        {
            String packageAsPath = excludePackage.getPackageName().replace(".", "/");
            if (path.contains(packageAsPath))
                return false;
        }

        boolean shouldScan = true;
        for (PackageModel includePackage : configuration.getScanJavaPackages())
        {
            String packageAsPath = includePackage.getPackageName().replace(".", "/");
            if (path.contains(packageAsPath))
            {
                shouldScan = true;
                break;
            }
            else
            {
                shouldScan = false;
            }
        }
        return shouldScan;
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
        WindupJavaConfigurationModel configuration = getJavaConfigurationModel(getGraphContext());
        for (PackageModel pkgModel : configuration.getExcludeJavaPackages())
        {
            String excludePkg = pkgModel.getPackageName();
            if (pkg.startsWith(excludePkg))
            {
                return false;
            }
        }

        // if the list is empty, assume it is intended to just accept all packages
        if (!configuration.getScanJavaPackages().iterator().hasNext())
        {
            return true;
        }

        for (PackageModel pkgModel : configuration.getScanJavaPackages())
        {
            String includePkg = pkgModel.getPackageName();
            if (pkg.startsWith(includePkg))
            {
                return true;
            }
        }

        return false;
    }


    /**
     * Checks whether the path is called "target" and has a pom.xml file hanging from its parent.
     *
     * @param file the {@link FileModel} to check
     * @return true if it's a target directory
     */
    public boolean isTargetDir(final FileModel file) {
        if (!file.getFileName().equals("target")) return false;

        return Stream.of(Optional.ofNullable(file.asFile().getParentFile().listFiles()).orElse(new File[0]))
                .anyMatch(f -> f.toPath().getFileName().toString().equals("pom.xml"));
    }
}
