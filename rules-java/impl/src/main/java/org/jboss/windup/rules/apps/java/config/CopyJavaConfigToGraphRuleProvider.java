package org.jboss.windup.rules.apps.java.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIContextProvider;
import org.jboss.forge.addon.ui.output.UIOutput;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.forge.furnace.util.Visitor;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.InitializationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.model.WindupJavaConfigurationModel;
import org.jboss.windup.rules.apps.java.service.WindupJavaConfigurationService;
import org.jboss.windup.util.PathUtil;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.file.FileSuffixPredicate;
import org.jboss.windup.util.file.FileVisit;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Copies configuration data from {@link GraphContext#getOptionMap()} to the graph itself for easy use by other {@link Rule}s.
 *
 */
public class CopyJavaConfigToGraphRuleProvider extends AbstractRuleProvider
{
    @Inject private UIContextProvider uIContextProvider;


    public CopyJavaConfigToGraphRuleProvider()
    {
        super(MetadataBuilder.forProvider(CopyJavaConfigToGraphRuleProvider.class)
                    .setPhase(InitializationPhase.class).setHaltOnException(true));
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        GraphOperation copyConfigToGraph = new GraphOperation()
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context)
            {
                Map<String, Object> config = event.getGraphContext().getOptionMap();
                Boolean sourceMode = (Boolean) config.get(SourceModeOption.NAME);

                @SuppressWarnings("unchecked")
                List<String> includeJavaPackages = (List<String>) config.get(ScanPackagesOption.NAME);

                warnIfScanPackagesTooGeneral(includeJavaPackages);

                @SuppressWarnings("unchecked")
                final List<String> excludeJavaPackages;
                if (config.get(ExcludePackagesOption.NAME) == null)
                    excludeJavaPackages = new ArrayList<>();
                else
                    excludeJavaPackages = new ArrayList<>((List<String>) config.get(ExcludePackagesOption.NAME));

                Predicate<File> predicate = new FileSuffixPredicate("\\.package-ignore\\.txt");
                Visitor<File> visitor = createExcludePackagesLoaderVisitor(excludeJavaPackages);

                FileVisit.visit(PathUtil.getUserIgnoreDir().toFile(), predicate, visitor);
                FileVisit.visit(PathUtil.getWindupIgnoreDir().toFile(), predicate, visitor);

                WindupJavaConfigurationModel javaCfg = WindupJavaConfigurationService
                        .getJavaConfigurationModel(event.getGraphContext());
                javaCfg.setSourceMode(sourceMode == null ? false : sourceMode);
                javaCfg.setScanJavaPackageList(includeJavaPackages);
                javaCfg.setExcludeJavaPackageList(excludeJavaPackages);
            }


            private void warnIfScanPackagesTooGeneral(List<String> includeJavaPackages)
            {
                if (includeJavaPackages != null)
                {
                    Set<String> tooGeneral = new HashSet(Arrays.asList("com org net".split(" ")));
                    for (String pkg : includeJavaPackages)
                    {
                        if (tooGeneral.contains(pkg))
                            continue;
                        return;
                    }
                }

                if (uIContextProvider == null || uIContextProvider.getUIContext() == null) // In a test
                    return;

                UIOutput uiOutput = uIContextProvider.getUIContext().getProvider().getOutput();
                uiOutput.warn(uiOutput.err(), "No packages were set in --" + ScanPackagesOption.NAME
                    + ". This will cause all .jar files to be decompiled and can possibly take a long time. "
                    + "Check the Windup User Guide for performance tips.");
            }


            private Visitor<File> createExcludePackagesLoaderVisitor(final List<String> excludeJavaPackages)
            {
                return new Visitor<File>()
                {
                    @Override
                    public void visit(File file)
                    {
                        try (FileInputStream inputStream = new FileInputStream(file))
                        {
                            LineIterator it = IOUtils.lineIterator(inputStream, "UTF-8");
                            while (it.hasNext())
                            {
                                String line = it.next();
                                if (line.startsWith("#") || line.trim().isEmpty())
                                    continue;
                                excludeJavaPackages.add(line);
                            }
                        }
                        catch (Exception e)
                        {
                            throw new WindupException("Failed loading package ignore patterns from [" + file.toString() + "]", e);
                        }
                    }
                };
            }

        };

        return ConfigurationBuilder.begin()
                .addRule()
                .perform(copyConfigToGraph);
    }
}
