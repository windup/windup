package org.jboss.windup.rule.groovy;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.Iterators;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.phase.ReportRenderingPhase;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.rulefilters.NotPredicate;
import org.jboss.windup.exec.rulefilters.RuleProviderPhasePredicate;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.java.JavaHintsClassificationsTest.TestHintsClassificationsTestRuleProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class GroovyExtensionJavaRulesTest
{
    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap.create(AddonArchive.class)
                    .addBeansXML()
                    .addClass(TestHintsClassificationsTestRuleProvider.class)
                    .addAsResource(new File("src/test/resources/groovy/GroovyClassificationsAndHints.windup.groovy"),
                                GROOVY_CLASSIFICATION_AND_HINT_FILE);
    }

    @Inject
    private WindupProcessor processor;

    public static String GROOVY_CLASSIFICATION_AND_HINT_FILE = "/org/jboss/windup/addon/groovy/GroovyClassificationsAndHints.windup.groovy";

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testHintsAndClassificationOperation() throws Exception
    {
        final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(),
                    "windup_" + RandomStringUtils.randomAlphanumeric(6));

        FileUtils.deleteDirectory(outputPath.toFile());
        Files.createDirectories(outputPath);

        try (GraphContext context = factory.create(outputPath.resolve("graph")))
        {
            String inputPath = "src/test/resources/org/jboss/windup/rules/java";

            ProjectModel pm = context.getFramed().addVertex(null, ProjectModel.class);
            pm.setName("Main Project");

            FileModel inputPathFrame = context.getFramed().addVertex(null, FileModel.class);
            inputPathFrame.setFilePath(inputPath);
            inputPathFrame.setProjectModel(pm);
            pm.setRootFileModel(inputPathFrame);

            FileModel fileModel = context.getFramed().addVertex(null, FileModel.class);
            fileModel.setFilePath(inputPath + "/JavaClassTestFile1.java");
            fileModel.setProjectModel(pm);

            pm.addFileModel(inputPathFrame);
            pm.addFileModel(fileModel);
            fileModel = context.getFramed().addVertex(null, FileModel.class);
            fileModel.setFilePath(inputPath + "/JavaClassTestFile2.java");
            fileModel.setProjectModel(pm);
            pm.addFileModel(fileModel);

            try
            {
                Predicate<RuleProvider> predicate = new NotPredicate(
                            new RuleProviderPhasePredicate(MigrationRulesPhase.class, ReportGenerationPhase.class, ReportRenderingPhase.class)
                            );
                WindupConfiguration configuration = new WindupConfiguration()
                            .setGraphContext(context)
                            .setRuleProviderFilter(predicate)
                            .setInputPath(Paths.get(inputPath))
                            .setOutputDirectory(outputPath)
                            .setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""))
                            .setOptionValue(SourceModeOption.NAME, true);

                processor.execute(configuration);

                GraphService<InlineHintModel> hintService = new GraphService<>(context, InlineHintModel.class);
                GraphService<ClassificationModel> classificationService = new GraphService<>(context,
                            ClassificationModel.class);

                GraphService<JavaTypeReferenceModel> typeRefService = new GraphService<>(context,
                            JavaTypeReferenceModel.class);
                Iterable<JavaTypeReferenceModel> typeReferences = typeRefService.findAll();
                Assert.assertTrue(typeReferences.iterator().hasNext());
                List<InlineHintModel> hints = Iterators.asList(hintService.findAll());
                Assert.assertEquals(4, hints.size());
                List<ClassificationModel> classifications = Iterators.asList(classificationService.findAll());
                Assert.assertEquals(1, classifications.size());

                Iterable<FileModel> fileModels = classifications.get(0).getFileModels();
                Assert.assertEquals(2, Iterators.asList(fileModels).size());
            }
            finally
            {
                FileUtils.deleteDirectory(outputPath.toFile());
            }
        }

    }

}