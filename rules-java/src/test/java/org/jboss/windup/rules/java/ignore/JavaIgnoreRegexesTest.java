package org.jboss.windup.rules.java.ignore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.util.Iterators;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysis;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.engine.predicates.RuleProviderWithDependenciesPredicate;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.config.classification.Classification;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.jboss.windup.rules.apps.java.scan.provider.AnalyzeJavaFilesRuleProvider;
import org.jboss.windup.rules.apps.java.scan.provider.IndexJavaSourceFilesRuleProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

@RunWith(Arquillian.class)
public class JavaIgnoreRegexesTest
{
    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClass(TestHintsClassificationsTestRuleProvider.class)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );

        return archive;
    }

    @Inject
    private TestHintsClassificationsTestRuleProvider provider;

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testHintsAndClassificationOperation() throws Exception
    {
        try (GraphContext context = factory.create())
        {

            Assert.assertNotNull(context);

            // Output dir.
            final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(),
                        "windup_" + RandomStringUtils.randomAlphanumeric(6));
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

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

                WindupConfiguration configuration = new WindupConfiguration()
                            .setGraphContext(context)
                            .setRuleProviderFilter(
                                        new RuleProviderWithDependenciesPredicate(
                                                    TestHintsClassificationsTestRuleProvider.class))
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

                Assert.assertEquals(4, provider.getTypeReferences().size());
                List<InlineHintModel> hints = Iterators.asList(hintService.findAll());
                Assert.assertEquals(4, hints.size());

                boolean foundAddonDep1 = false;
                boolean foundAddonDep2 = false;
                boolean foundIterators = false;
                boolean foundCallables = false;
                for (InlineHintModel hint : hints)
                {
                    System.out.println("Hint: " + hint.getHint());
                    if (hint.getHint().contains("AddonDependencyEntry"))
                    {
                        if (!foundAddonDep1)
                        {
                            foundAddonDep1 = true;
                        }
                        else if (!foundAddonDep2)
                        {
                            foundAddonDep2 = true;
                        }
                        else
                        {
                            Assert.fail("Found too many references to AddonDependencyEntry");
                        }
                    }
                    else if (hint.getHint().contains("util.Callables"))
                    {
                        foundCallables = true;
                    }
                    else if (hint.getHint().contains("util.Iterators"))
                    {
                        foundIterators = true;
                    }
                }
                Assert.assertTrue(foundAddonDep1);
                Assert.assertTrue(foundAddonDep2);
                Assert.assertTrue(foundIterators);
                Assert.assertTrue(foundCallables);

                List<ClassificationModel> classifications = Iterators.asList(classificationService.findAll());
                Assert.assertEquals(1, classifications.size());
                classifications.get(0).getDescription().contains("JavaClassTestFile");

                Iterable<FileModel> fileModels = classifications.get(0).getFileModels();
                Assert.assertEquals(2, Iterators.asList(fileModels).size());
            }
            finally
            {
                FileUtils.deleteDirectory(outputPath.toFile());
            }
        }

    }

    @Singleton
    public static class TestHintsClassificationsTestRuleProvider extends WindupRuleProvider
    {
        private Set<JavaTypeReferenceModel> typeReferences = new HashSet<>();

        @Override
        public Class<? extends RulePhase> getPhase()
        {
            return InitialAnalysis.class;
        }

        @Override
        public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
        {
            return asClassList(AnalyzeJavaFilesRuleProvider.class, IndexJavaSourceFilesRuleProvider.class);
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            AbstractIterationOperation<JavaTypeReferenceModel> addTypeRefToList = new AbstractIterationOperation<JavaTypeReferenceModel>()
            {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload)
                {
                    typeReferences.add(payload);
                }
            };
            
            return ConfigurationBuilder.begin()
            .addRule()
            .when(JavaClass.references("org.jboss.forge.furnace.{name}").inType("{file}{suffix}").at(TypeReferenceLocation.IMPORT))
            .perform(
                Classification.as("Furnace Service {file}").withDescription("Described by {file}").with(Link.to("JBoss Forge", "http://forge.jboss.org")).withEffort(0)
                    .and(Hint.withText("Furnace type references (such as {name}) imply that the client code must be run within a Furnace container.")
                             .withEffort(8)
                    .and(addTypeRefToList))
            )
            .where("suffix").matches("\\d");

        }
        // @formatter:on

        public Set<JavaTypeReferenceModel> getTypeReferences()
        {
            return typeReferences;
        }
    }

}
