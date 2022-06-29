package org.jboss.windup.rules.apps.condition;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
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
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.java.scan.ast.AnalyzeJavaFilesRuleProvider;
import org.jboss.windup.rules.files.condition.FileContent;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;

@RunWith(Arquillian.class)
public class FileContentTest {
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
                .addAsResource("xml/FileContentXmlExample.windup.xml");

        return archive;
    }

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Inject
    private FileContentTestRuleProvider provider;

    @Test
    public void testFileContentScan() throws Exception {
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
            windupConfiguration.setOptionValue(SourceModeOption.NAME, true);
            processor.execute(windupConfiguration);

            InlineHintService inlineHintService = new InlineHintService(context);
            Iterable<InlineHintModel> inlineHints = inlineHintService.findAll();
            boolean foundFileContentHintFromXmlRule = false;
            for (InlineHintModel inlineHint : inlineHints) {
                if (inlineHint.getHint().equals("File Content xml")) {
                    foundFileContentHintFromXmlRule = true;
                }
            }
            Assert.assertTrue(foundFileContentHintFromXmlRule);

            boolean foundFile1Line1 = false;
            boolean foundFile1Line2 = false;
            boolean foundFile2Line1 = false;
            boolean foundFile2Line2 = false;
            boolean foundFile2Line3 = false;
            boolean foundFile2Line4 = false;
            boolean foundFile3Needle = false;
            for (int i = 0; i < provider.rule1ResultStrings.size(); i++) {
                FileLocationModel location = provider.rule1ResultModels.get(i);
                System.out.println("Location: " + location);
                if (location.getFile().getFileName().equals("file1.txt")) {
                    if (location.getLineNumber() == 1 && location.getColumnNumber() == 8 && location.getSourceSnippit().equals("file 1.")) {
                        foundFile1Line1 = true;
                    } else if (location.getLineNumber() == 2 && location.getColumnNumber() == 27 && location.getSourceSnippit().equals("file 1.")) {
                        foundFile1Line2 = true;
                    }
                } else if (location.getFile().getFileName().equals("file2.txt")) {
                    if (location.getLineNumber() == 1 && location.getColumnNumber() == 5 && location.getSourceSnippit().equals("file firstline2.")) {
                        foundFile2Line1 = true;
                    } else if (location.getLineNumber() == 2 && location.getColumnNumber() == 8 && location.getSourceSnippit().equals("file #2.")) {
                        foundFile2Line2 = true;
                    } else if (location.getLineNumber() == 3 && location.getColumnNumber() == 5 && location.getSourceSnippit().equals("file 2.")) {
                        foundFile2Line3 = true;
                    } else if (location.getLineNumber() == 4 && location.getColumnNumber() == 0
                            && location.getSourceSnippit().equals("file lastline2.")) {
                        foundFile2Line4 = true;
                    }
                } else {
                    Assert.fail("Unrecognized file: " + location.getFile().getFileName());
                }
            }
            Assert.assertTrue(foundFile1Line1);
            Assert.assertTrue(foundFile1Line2);
            Assert.assertTrue(foundFile2Line1);
            Assert.assertTrue(foundFile2Line2);
            Assert.assertTrue(foundFile2Line3);
            Assert.assertTrue(foundFile2Line4);

            for (int i = 0; i < provider.rule2ResultStrings.size(); i++) {
                FileLocationModel location = provider.rule2ResultModels.get(i);
                System.out.println("Rule 2 Location: " + location);
                if (location.getFile().getFileName().equals("file3.txt") && location.getLineNumber() == 30721 && location.getColumnNumber() == 15) {
                    foundFile3Needle = true;
                }
            }
            Assert.assertTrue(foundFile3Needle);

            Assert.assertEquals(1, provider.count);

            Assert.assertEquals(1L, provider.rule3ResultStrings.size());
            Assert.assertEquals("NEEDLE", provider.rule3ResultStrings.get(0));
            Assert.assertEquals(1L, provider.rule3ResultModels.size());
            final FileLocationModel fileLocationModel = provider.rule3ResultModels.get(0);
            Assert.assertEquals("file3.txt", fileLocationModel.getFile().getFileName());
            Assert.assertEquals(30721, fileLocationModel.getLineNumber());
            Assert.assertEquals(15, fileLocationModel.getColumnNumber());
        }
    }

    @Singleton
    @RuleMetadata(phase = InitialAnalysisPhase.class, after = AnalyzeJavaFilesRuleProvider.class)
    public static class FileContentTestRuleProvider extends AbstractRuleProvider {
        private final List<String> rule1ResultStrings = new ArrayList<>();
        private final List<FileLocationModel> rule1ResultModels = new ArrayList<>();
        private final List<String> rule2ResultStrings = new ArrayList<>();
        private final List<FileLocationModel> rule2ResultModels = new ArrayList<>();
        private final List<String> rule3ResultStrings = new ArrayList<>();
        private final List<FileLocationModel> rule3ResultModels = new ArrayList<>();
        public int count = 0;

        // @formatter:off
        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {

            return ConfigurationBuilder.begin()
                    .addRule()
                    .when(FileContent.matches("file {text}.").inFileNamed("{*}.txt"))
                    .perform(new ParameterizedIterationOperation<FileLocationModel>() {
                        private final RegexParameterizedPatternParser textPattern = new RegexParameterizedPatternParser("{text}");

                        @Override
                        public void performParameterized(GraphRewrite event, EvaluationContext context, FileLocationModel payload) {
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
                    })
                    .addRule()
                    .when(FileContent.matches(" THE {needle} IN THE HAYSTACK {*}").inFileNamed("{*}.txt"))
                    .perform(new ParameterizedIterationOperation<FileLocationModel>() {
                        private final RegexParameterizedPatternParser textPattern = new RegexParameterizedPatternParser("{needle}");

                        @Override
                        public void performParameterized(GraphRewrite event, EvaluationContext context, FileLocationModel payload) {
                            rule2ResultStrings.add(textPattern.getBuilder().build(event, context));
                            rule2ResultModels.add(payload);
                        }

                        @Override
                        public Set<String> getRequiredParameterNames() {
                            return textPattern.getRequiredParameterNames();
                        }

                        @Override
                        public void setParameterStore(ParameterStore store) {
                            textPattern.setParameterStore(store);
                        }
                    })
                    .addRule()
                    .when(JavaClass.references("java.io.IOException").at(TypeReferenceLocation.IMPORT).as("1")
                            .and(FileContent.from("1").matches("this is file").as("2")))
                    .perform(Iteration.over("2").perform(new AbstractIterationOperation<FileLocationModel>() {

                                                             @Override
                                                             public void perform(GraphRewrite event, EvaluationContext context, FileLocationModel payload) {
                                                                 count++;
                                                             }
                                                         }
                    ).endIteration())
                    .addRule()
                    .when(FileContent.matches(" THE {foo} IN THE HAYSTACK {*}"))
                    .perform(new ParameterizedIterationOperation<FileLocationModel>() {
                        private final RegexParameterizedPatternParser textPattern = new RegexParameterizedPatternParser("{foo}");

                        @Override
                        public void performParameterized(GraphRewrite event, EvaluationContext context, FileLocationModel payload) {
                            rule3ResultStrings.add(textPattern.getBuilder().build(event, context));
                            rule3ResultModels.add(payload);
                        }

                        @Override
                        public Set<String> getRequiredParameterNames() {
                            return textPattern.getRequiredParameterNames();
                        }

                        @Override
                        public void setParameterStore(ParameterStore store) {
                            textPattern.setParameterStore(store);
                        }
                    })
                    .where("foo").matches("NEEDLE");
        }
        // @formatter:on
    }

}
