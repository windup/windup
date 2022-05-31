package org.jboss.windup.rules.apps.condition;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.parameters.ParameterizedIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.rulefilters.NotPredicate;
import org.jboss.windup.exec.rulefilters.RuleProviderPhasePredicate;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.FileReferenceModel;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.rules.files.condition.File;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RunWith(Arquillian.class)
public class FileTest {
    @Inject
    private WindupProcessor processor;
    @Inject
    private GraphContextFactory factory;
    @Inject
    private FileContentTestRuleProvider provider;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-xml"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-base"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        final AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addAsResource("xml/FileXmlExample.windup.xml");

        return archive;
    }

    @Test
    public void testFileScan() throws Exception {
        try (GraphContext context = factory.create(true)) {
            Path inputPath = Paths.get("src/test/resources/");

            Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "windup_"
                    + UUID.randomUUID().toString());
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            Predicate<RuleProvider> predicate = new NotPredicate(new RuleProviderPhasePredicate(ReportGenerationPhase.class,
                    MigrationRulesPhase.class));

            WindupConfiguration windupConfiguration = new WindupConfiguration()
                    .setRuleProviderFilter(predicate)
                    .setGraphContext(context);
            windupConfiguration.addInputPath(inputPath);
            windupConfiguration.setOutputDirectory(outputPath);
            processor.execute(windupConfiguration);

            ClassificationService classificationService = new ClassificationService(context);
            Iterable<ClassificationModel> classifications = classificationService.findAll();
            int count = 0;
            for (ClassificationModel classification : classifications) {
                count++;
            }
            //test the classifications xml windup.xml rules
            Assert.assertEquals(3, count);
            //test the matches from the java rules
            Assert.assertTrue(provider.rule1ResultStrings.contains("file1"));
            Assert.assertTrue(provider.rule1ResultStrings.contains("file2"));
            Assert.assertTrue(provider.rule1ResultStrings.contains("file3"));
        }
    }

    @Singleton
    public static class FileContentTestRuleProvider extends AbstractRuleProvider {
        private List<String> rule1ResultStrings = new ArrayList<>();
        private List<FileReferenceModel> rule1ResultModels = new ArrayList<>();

        public FileContentTestRuleProvider() {
            super(MetadataBuilder.forProvider(FileContentTestRuleProvider.class)
                    .setPhase(InitialAnalysisPhase.class).setExecuteAfterIDs(Collections.singletonList("AnalyzeJavaFilesRuleProvider")));
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin()
                    .addRule()
                    .when(File.inFileNamed("{text}.txt"))
                    .perform(new ParameterizedIterationOperation<FileReferenceModel>() {
                        private RegexParameterizedPatternParser textPattern = new RegexParameterizedPatternParser("{text}");

                        @Override
                        public void performParameterized(GraphRewrite event, EvaluationContext context, FileReferenceModel payload) {
                            rule1ResultStrings.add(textPattern.getBuilder().build(event, context));
                            rule1ResultModels.add(payload);
                        }

                        @Override
                        public Set<String> getRequiredParameterNames() {
                            return textPattern.getRequiredParameterNames();
                        }

                        @Override
                        public void setParameterStore(ParameterStore store) {
                            textPattern.setParameterStore(store);
                        }
                    });
        }
        // @formatter:on
    }

}