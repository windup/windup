package org.jboss.windup.rules.java;

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
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.engine.WindupConfiguration;
import org.jboss.windup.engine.WindupProcessor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.GraphLifecycleListener;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.config.Classification;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.rules.apps.java.config.JavaClass;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.provider.AnalyzeJavaFilesRuleProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

@RunWith(Arquillian.class)
public class HintsClassificationsTest
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
    public void testIterationVariableResolving() throws Exception
    {
        try (GraphContext context = factory.create())
        {

            Assert.assertNotNull(context);

            // Output dir.
            final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(),
                        "windup_" + RandomStringUtils.randomAlphanumeric(6));
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            // Data filler.
            GraphLifecycleListener gll = new GraphLifecycleListener()
            {
                private static final String INPUT_PATH = "src/test/java/org/jboss/windup/rules/java";

                public void postOpen(GraphContext context)
                {
                    ProjectModel pm = context.getFramed().addVertex(null, ProjectModel.class);
                    pm.setName("Main Project");

                    FileModel inputPathFrame = context.getFramed().addVertex(null, FileModel.class);
                    inputPathFrame.setFilePath(INPUT_PATH);
                    inputPathFrame.setProjectModel(pm);
                    pm.setRootFileModel(inputPathFrame);

                    FileModel fileModel = context.getFramed().addVertex(null, FileModel.class);
                    fileModel.setFilePath(INPUT_PATH + "/HintsClassificationsTest.java");
                    fileModel.setProjectModel(pm);

                    pm.addFileModel(inputPathFrame);
                    pm.addFileModel(fileModel);
                    fileModel = context.getFramed().addVertex(null, FileModel.class);
                    fileModel.setFilePath(INPUT_PATH + "/JavaClassTest.java");
                    fileModel.setProjectModel(pm);
                    pm.addFileModel(fileModel);

                    // WindupConfigModel.
                    // I am coming to conclusion that we need a WindupConfig
                    // which doesn't need context.
                    WindupConfigurationModel config = GraphService.getConfigurationModel(context);
                    config.setScanJavaPackageList(Collections.singletonList(""));
                    config.setInputPath(inputPathFrame); // Must be the same frame!
                    config.setSourceMode(true);
                    config.setOutputPath(outputPath.toString());
                }

                public void preShutdown(GraphContext context)
                {
                }
            };

            try
            {

                try
                {
                    // TODO: Consolidate the config - e.g. the outputPath is now set at 2 places.
                    WindupConfiguration configuration = new WindupConfiguration()
                                .setGraphContext(context)
                                .setGraphListener(gll);
                    processor.execute(configuration);
                }
                catch (Exception e)
                {
                    if (e.getMessage() == null || !e.getMessage().contains("CreateMainApplicationReport"))
                        throw e;
                }

                GraphService<InlineHintModel> hintService = new GraphService<>(context, InlineHintModel.class);
                GraphService<ClassificationModel> classificationService = new GraphService<>(context,
                            ClassificationModel.class);

                GraphService<TypeReferenceModel> typeRefService = new GraphService<>(context, TypeReferenceModel.class);
                Iterable<TypeReferenceModel> typeReferences = typeRefService.findAll();
                Assert.assertTrue(typeReferences.iterator().hasNext());

                Assert.assertEquals(4, provider.getTypeReferences().size());
                List<InlineHintModel> hints = Iterators.asList(hintService.findAll());
                Assert.assertEquals(4, hints.size());
                List<ClassificationModel> classifications = Iterators.asList(classificationService.findAll());
                Assert.assertEquals(1, classifications.size());

                @SuppressWarnings("unused")
                Iterable<FileModel> fileModels = classifications.get(0).getFileModels();

                // TODO fix this: there is a file multiple times:
                // Assert.assertEquals(2, Iterators.asList(fileModels).size());
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
        private Set<TypeReferenceModel> typeReferences = new HashSet<>();

        @Override
        public RulePhase getPhase()
        {
            return RulePhase.INITIAL_ANALYSIS;
        }

        @Override
        public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
        {
            return asClassList(AnalyzeJavaFilesRuleProvider.class);
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            AbstractIterationOperation<TypeReferenceModel> addTypeRefToList = new AbstractIterationOperation<TypeReferenceModel>()
            {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context, TypeReferenceModel payload)
                {
                    typeReferences.add(payload);
                }
            };
            
            return ConfigurationBuilder.begin()
            .addRule()
            .when(JavaClass.references("org.jboss.forge.furnace.*").at(TypeReferenceLocation.IMPORT))
            .perform(
                Classification.as("Furnace Service").with(Link.to("JBoss Forge", "http://forge.jboss.org")).withEffort(0)
                    .and(Hint.withText("Furnace type references imply that the client code must be run within a Furnace container.")
                             .withEffort(8)
                    .and(addTypeRefToList))
            );

        }
        // @formatter:on

        public Set<TypeReferenceModel> getTypeReferences()
        {
            return typeReferences;
        }
    }

}
