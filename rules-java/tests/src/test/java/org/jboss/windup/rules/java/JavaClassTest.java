package org.jboss.windup.rules.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleSubset;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.engine.predicates.RuleProviderWithDependenciesPredicate;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.provider.AnalyzeJavaFilesRuleProvider;
import org.jboss.windup.testutil.basics.WindupTestUtilMethods;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

@RunWith(Arquillian.class)
public class JavaClassTest
{
    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
                @AddonDependency(name = "org.jboss.windup.tests:test-util"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Inject
    JavaClassTestRuleProvider provider;

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testJavaClassCondition() throws IOException, InstantiationException, IllegalAccessException
    {
        try (GraphContext context = factory.create(WindupTestUtilMethods.getTempDirectoryForGraph()))
        {
            final String inputDir = "src/test/resources/org/jboss/windup/rules/java";

            final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(),
                        "windup_" + RandomStringUtils.randomAlphanumeric(6));
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            ProjectModel pm = context.getFramed().addVertex(null, ProjectModel.class);
            pm.setName("Main Project");

            FileModel inputPathFrame = context.getFramed().addVertex(null, FileModel.class);
            inputPathFrame.setFilePath(inputDir);
            inputPathFrame.setProjectModel(pm);
            pm.addFileModel(inputPathFrame);

            pm.setRootFileModel(inputPathFrame);

            FileModel fileModel = context.getFramed().addVertex(null, FileModel.class);
            fileModel.setFilePath(inputDir + "/JavaClassTestFile1.java");
            fileModel.setProjectModel(pm);
            pm.addFileModel(fileModel);

            fileModel = context.getFramed().addVertex(null, FileModel.class);
            fileModel.setFilePath(inputDir + "/JavaClassTestFile2.java");
            fileModel.setProjectModel(pm);
            pm.addFileModel(fileModel);

            context.getGraph().getBaseGraph().commit();

            final WindupConfiguration processorConfig = new WindupConfiguration();
            processorConfig.setRuleProviderFilter(new RuleProviderWithDependenciesPredicate(
                        JavaClassTestRuleProvider.class));
            processorConfig.setGraphContext(context);
            processorConfig.addInputPath(Paths.get(inputDir));
            processorConfig.setOutputDirectory(outputPath);
            processorConfig.setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""));
            processorConfig.setOptionValue(SourceModeOption.NAME, true);

            processor.execute(processorConfig);

            GraphService<JavaTypeReferenceModel> typeRefService = new GraphService<>(context,
                        JavaTypeReferenceModel.class);
            Iterable<JavaTypeReferenceModel> typeReferences = typeRefService.findAll();
            Assert.assertTrue(typeReferences.iterator().hasNext());

            Assert.assertEquals(4, provider.getFirstRuleMatchCount());
            Assert.assertEquals(2, provider.getSecondRuleMatchCount());
            Assert.assertEquals(13, provider.getThirdRuleMatchCount());
        }
    }

    /**
     * Testing that .from() and .as() sets the right variable
     */
    @Test
    public void testJavaClassInputOutputVariables()
    {
        JavaClass as = (JavaClass) JavaClass.from("input").references("abc").as("output");
        Assert.assertEquals("input", as.getInputVariablesName());
        Assert.assertEquals("output", as.getOutputVariablesName());
    }

    private static Path getDefaultPath()
    {
        return FileUtils.getTempDirectory().toPath().resolve("Windup")
                    .resolve("windupgraph_javaclasstest_" + RandomStringUtils.randomAlphanumeric(6));
    }

    @Singleton
    public static class JavaClassTestRuleProvider extends AbstractRuleProvider
    {
        private static final Logger log = Logger.getLogger(RuleSubset.class.getName());

        private int firstRuleMatchCount = 0;
        private int secondRuleMatchCount = 0;
        private int thirdRuleMatchCount = 0;


        public JavaClassTestRuleProvider()
        {
            super(MetadataBuilder.forProvider(JavaClassTestRuleProvider.class)
                        .addExecuteAfter(AnalyzeJavaFilesRuleProvider.class));
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            return ConfigurationBuilder.begin()
            .addRule().when(
                JavaClass.references("org.jboss.forge.furnace.{*}").inType("{*}").at(TypeReferenceLocation.IMPORT).as("1").and(
                            JavaClass.from("1").references("org{*}").at(TypeReferenceLocation.IMPORT).as("2")
                )
            ).perform(
                Iteration.over("1").perform(new AbstractIterationOperation<JavaTypeReferenceModel>()
                {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload)
                    {
                        firstRuleMatchCount++;
                        log.info("First rule matched: " + payload.getFile().getFilePath());
                    }
                }).endIteration()
            )

            .addRule().when(
                JavaClass.references("org.jboss.forge.furnace.{*}").inType("{*}JavaClassTestFile1").at(TypeReferenceLocation.IMPORT)
            ).perform(
                Iteration.over().perform(new AbstractIterationOperation<JavaTypeReferenceModel>()
                {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload)
                    {
                        secondRuleMatchCount++;
                    }
                }).endIteration()
            )

                        //test iteration over matched file
            .addRule().when(
                JavaClass.references("org.jboss.forge.furnace.{*}").inType("{*}").at(TypeReferenceLocation.IMPORT).as("1").and(
                JavaClass.from("1").references("org{*}").at(TypeReferenceLocation.IMPORT).as("2")
                )
            ).perform(
                Iteration.over("2").perform(new AbstractIterationOperation<JavaTypeReferenceModel>()
                {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload)
                    {
                        thirdRuleMatchCount++;
                        log.info("First rule matched: " + payload.getFile().getFilePath());
                    }
                }).endIteration()
            );
        }
        // @formatter:on

        public int getFirstRuleMatchCount()
        {
            return firstRuleMatchCount;
        }

        public int getSecondRuleMatchCount()
        {
            return secondRuleMatchCount;
        }
        public int getThirdRuleMatchCount()
        {
            return thirdRuleMatchCount;
        }

    }
}
