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
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
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
import org.jboss.windup.graph.model.resource.ResourceModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.provider.AnalyzeJavaFilesRuleProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

@RunWith(Arquillian.class)
public class JavaClassSourceMatchTest
{
    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClass(JavaClassTestRuleProvider.class)
                    .addClass(JavaClassSourceMatchTest.class)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:windup-rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );

        return archive;
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
        try (GraphContext context = factory.create(getDefaultPath()))
        {
            final String inputDir = "src/test/resources/org/jboss/windup/rules/java";

            final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(),
                        "windup_" + RandomStringUtils.randomAlphanumeric(6));
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            ProjectModel pm = context.getFramed().addVertex(null, ProjectModel.class);
            pm.setName("Main Project");

            ResourceModel inputPathFrame = context.getFramed().addVertex(null, ResourceModel.class);
            inputPathFrame.setFilePath(inputDir);
            inputPathFrame.setProjectModel(pm);
            pm.addResourceModel(inputPathFrame);

            pm.setRootResourceModel(inputPathFrame);

            ResourceModel fileModel = context.getFramed().addVertex(null, ResourceModel.class);
            fileModel.setFilePath(inputDir + "/JavaClassTestFile1.java");
            fileModel.setProjectModel(pm);
            pm.addResourceModel(fileModel);

            fileModel = context.getFramed().addVertex(null, ResourceModel.class);
            fileModel.setFilePath(inputDir + "/JavaClassTestFile2.java");
            fileModel.setProjectModel(pm);
            pm.addResourceModel(fileModel);

            context.getGraph().getBaseGraph().commit();

            final WindupConfiguration processorConfig = new WindupConfiguration();
            processorConfig.setRuleProviderFilter(new RuleProviderWithDependenciesPredicate(
                        JavaClassTestRuleProvider.class));
            processorConfig.setGraphContext(context);
            processorConfig.setInputPath(Paths.get(inputDir));
            processorConfig.setOutputDirectory(outputPath);
            processorConfig.setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""));

            processor.execute(processorConfig);

            GraphService<JavaTypeReferenceModel> typeRefService = new GraphService<>(context,
                        JavaTypeReferenceModel.class);
            Iterable<JavaTypeReferenceModel> typeReferences = typeRefService.findAll();
            Assert.assertTrue(typeReferences.iterator().hasNext());

            Assert.assertEquals(1, provider.getFirstRuleMatchCount());
            Assert.assertEquals(1, provider.getSecondRuleMatchCount());
            Assert.assertEquals(1, provider.getThirdRuleMatchCount());
            Assert.assertEquals(1, provider.getFourthRuleMatchCount());
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

    private Path getDefaultPath()
    {
        return FileUtils.getTempDirectory().toPath().resolve("Windup")
                    .resolve("windupgraph_javaclasstest_" + RandomStringUtils.randomAlphanumeric(6));
    }

    @Singleton
    public static class JavaClassTestRuleProvider extends AbstractRuleProvider
    {
        private static Logger log = Logger.getLogger(RuleSubset.class.getName());

        private int thirdRuleMatchCount = 0;
        private int firstRuleMatchCount = 0;
        private int secondRuleMatchCount = 0;
        private int fourthRuleMatchCount = 0;

        public JavaClassTestRuleProvider()
        {
            super(MetadataBuilder.forProvider(JavaClassTestRuleProvider.class).addExecuteAfter(AnalyzeJavaFilesRuleProvider.class));
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            return ConfigurationBuilder.begin()
            .addRule().when(JavaClass.references("org.jboss.windup.graph.model.resource.ResourceModel.setFilePath{*}").matchesSource("{*}/JavaHintsClassificationsTest.java{*}").inType("{*}").at(TypeReferenceLocation.METHOD_CALL))
            .perform( Iteration.over().perform(new AbstractIterationOperation<JavaTypeReferenceModel>()
                        {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload)
                {
                    firstRuleMatchCount++;
                }
            }).endIteration())
            .addRule()
            .when(JavaClass.references("org.jboss.windup.rules.java.JavaClassTestFile1.testJavaClassCondition()").matchesSource("{*}{line}{*}").inType("{*}").at(TypeReferenceLocation.METHOD))
            .perform( Iteration.over().perform(new AbstractIterationOperation<JavaTypeReferenceModel>()
                        {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload)
                {
                    secondRuleMatchCount++;
                }
            }).endIteration())
            .where("line").matches("testJavaClassCondition\\(\\) throws IOException, InstantiationException")
            
            .addRule()
            .when(JavaClass.references("org.jboss.windup.exec.configuration.WindupConfiguration.setRuleProviderFilter{*}").matchesSource("{*}{line}{*}").at(TypeReferenceLocation.METHOD_CALL))
            .perform( Iteration.over().perform(new AbstractIterationOperation<JavaTypeReferenceModel>()
                        {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload)
                {
                    thirdRuleMatchCount++;
                }
            }).endIteration())
            .where("line").matches(".setRuleProviderFilter\\(new")
            .addRule()
            .when(JavaClass.references("org.jboss.windup.rules.java.JavaClassTestFile2").matchesSource("{*}{line}{*}").at(TypeReferenceLocation.TYPE))
            .perform( Iteration.over().perform(new AbstractIterationOperation<JavaTypeReferenceModel>()
                        {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload)
                {
                    fourthRuleMatchCount++;
                }
            }).endIteration())
            .where("line").matches("public class JavaClassTestFile2")
            ;
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
        
        public int getFourthRuleMatchCount()
        {
            return fourthRuleMatchCount;
        }
    }
}
