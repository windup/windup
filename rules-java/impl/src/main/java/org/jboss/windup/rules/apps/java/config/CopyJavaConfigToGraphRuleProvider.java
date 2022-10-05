package org.jboss.windup.rules.apps.java.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.forge.furnace.util.Visitor;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.InitializationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
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
 */
@RuleMetadata(phase = InitializationPhase.class, haltOnException = true)
public class CopyJavaConfigToGraphRuleProvider extends AbstractRuleProvider {
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        GraphOperation copyConfigToGraph = new GraphOperation() {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context) {
                Map<String, Object> config = event.getGraphContext().getOptionMap();
                Boolean sourceMode = (Boolean) config.get(SourceModeOption.NAME);
                Boolean enableClassFoundFoundAnalysis = (Boolean) config.get(EnableClassNotFoundAnalysisOption.NAME);

                @SuppressWarnings("unchecked")
                List<String> includeJavaPackages = (List<String>) config.get(ScanPackagesOption.NAME);

                @SuppressWarnings("unchecked") final List<String> excludeJavaPackages;
                if (config.get(ExcludePackagesOption.NAME) == null)
                    excludeJavaPackages = new ArrayList<>();
                else
                    excludeJavaPackages = new ArrayList<>((List<String>) config.get(ExcludePackagesOption.NAME));

                Predicate<File> predicate = new FileSuffixPredicate("\\.package-ignore\\.txt");
                Visitor<File> visitor = new Visitor<File>() {
                    @Override
                    public void visit(File file) {
                        try (FileInputStream inputStream = new FileInputStream(file)) {
                            LineIterator it = IOUtils.lineIterator(inputStream, "UTF-8");
                            while (it.hasNext()) {
                                String line = it.next();
                                if (!line.startsWith("#") && !line.trim().isEmpty()) {
                                    excludeJavaPackages.add(line);
                                }
                            }
                        } catch (Exception e) {
                            throw new WindupException("Failed loading package ignore patterns from [" + file.toString() + "]", e);
                        }
                    }
                };

                FileVisit.visit(PathUtil.getUserIgnoreDir().toFile(), predicate, visitor);
                FileVisit.visit(PathUtil.getWindupIgnoreDir().toFile(), predicate, visitor);

                WindupJavaConfigurationModel javaConfiguration = WindupJavaConfigurationService.getJavaConfigurationModel(event
                        .getGraphContext());
                javaConfiguration.setSourceMode(sourceMode == null ? false : sourceMode);
                javaConfiguration.setScanJavaPackageList(includeJavaPackages);
                javaConfiguration.setExcludeJavaPackageList(excludeJavaPackages);
                javaConfiguration.setClassNotFoundAnalysisEnabled(enableClassFoundFoundAnalysis == null ? false : enableClassFoundFoundAnalysis);

                List<File> additionalClasspaths = (List<File>) config.get(AdditionalClasspathOption.NAME);
                if (additionalClasspaths != null) {
                    FileService fileService = new FileService(event.getGraphContext());
                    for (File file : additionalClasspaths) {
                        FileModel fileModel = fileService.createByFilePath(file.getAbsolutePath());
                        javaConfiguration.addAdditionalClasspath(fileModel);
                    }
                }
            }
        };

        return ConfigurationBuilder.begin()
                .addRule()
                .perform(copyConfigToGraph);
    }
}
