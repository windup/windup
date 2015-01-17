package org.jboss.windup.reporting.rules.rendering;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.addons.AddonDependency;
import org.jboss.forge.furnace.addons.AddonFilter;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportRendering;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.service.ReportService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class CssJsResourceRenderingRuleProvider extends WindupRuleProvider
{

    @Inject
    private Addon addon;
    @Inject
    private Furnace furnace;

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return ReportRendering.class;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        GraphOperation copyCssOperation = new GraphOperation()
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context)
            {
                final WindupConfigurationModel cfg = WindupConfigurationService.getConfigurationModel(event
                            .getGraphContext());
                String outputPath = cfg.getOutputPath().getFilePath();
                copyCssResourcesToOutput(event.getGraphContext(), outputPath);
            }

            @Override
            public String toString()
            {
                return "CopyCSSToOutput";
            }
        };

        Configuration configuration = ConfigurationBuilder.begin()
                    .addRule()
                    .perform(copyCssOperation);
        return configuration;
    }

    private void copyCssResourcesToOutput(GraphContext context, String outputDir)
    {
        ReportService reportService = new ReportService(context);
        Path outputPath = Paths.get(reportService.getReportDirectory(), "resources");

        // iterate through the addons to scan
        for (Addon addonToScan : getAddonsToScan())
        {
            List<File> addonResources = addon.getRepository().getAddonResources(addonToScan.getId());
            for (File addonResource : addonResources)
            {
                try
                {
                    if (addonResource.isDirectory())
                    {
                        Path addonReportsResourcesPath = addonResource.toPath().resolve("reports").resolve("resources");
                        if (Files.isDirectory(addonReportsResourcesPath))
                        {
                            recursePath(addonReportsResourcesPath, outputPath);
                        }
                    }
                    else
                    {
                        try (FileSystem fs = FileSystems.newFileSystem(addonResource.toPath(),
                                    addonToScan.getClassLoader()))
                        {
                            Path p = fs.getPath("reports", "resources");
                            try
                            {
                                recursePath(p, outputPath);
                            }
                            catch (NoSuchFileException e)
                            {
                                // ignore ... this just means this archive did not contain report resources
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    throw new RuntimeException("Exception reading resource.", e);
                }
            }
        }
    }

    private void recursePath(final Path path, final Path resultPath) throws IOException
    {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
            {
                String relativePath = StringUtils.substringAfter(file.toString(), path.toString());

                // needed on windows, as for some reason the path from a zip still uses s'/' sometimes
                relativePath = FilenameUtils.separatorsToSystem(relativePath);

                relativePath = StringUtils.removeStart(relativePath, File.separator);
                Path resultFile = resultPath.resolve(relativePath);

                FileUtils.forceMkdir(resultFile.getParent().toFile());
                Files.copy(file, resultFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Get all {@link Addon} instances that depend on the reporting addon
     */
    private Set<Addon> getAddonsToScan()
    {
        AddonFilter filter = new AddonFilter()
        {
            @Override
            public boolean accept(Addon addon)
            {
                // make sure to include ourselves as well (even though we don't technically depend on ourselves)
                return addonDependsOnReporting(addon) || addon.equals(CssJsResourceRenderingRuleProvider.this.addon);
            }
        };

        return furnace.getAddonRegistry().getAddons(filter);
    }

    /**
     * Returns true if the addon depends on reporting.
     */
    private boolean addonDependsOnReporting(Addon addon)
    {
        for (AddonDependency dep : addon.getDependencies())
        {
            if (dep.getDependency().equals(this.addon))
            {
                return true;
            }
            boolean subDep = addonDependsOnReporting(dep.getDependency());
            if (subDep)
            {
                return true;
            }
        }
        return false;
    }

}
