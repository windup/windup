package org.jboss.windup.rules.java;

import org.jboss.windup.graph.GraphLifecycleListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.engine.WindupProcessor;
import org.jboss.windup.engine.WindupProcessorConfig;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.PackageModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.config.JavaClass;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.provider.AnalyzeJavaFilesRuleProvider;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

@RunWith(Arquillian.class)
public class JavaClassTest
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
                    .addClass(TestJavaClassTestRuleProvider.class)
                    .addClass(JavaClassTest.class)
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
    TestJavaClassTestRuleProvider provider;

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContext context;

    @Test
    public void testIterationVariableResolving() throws IOException
    {
        final String inputDir = "src/test/java/org/jboss/windup/rules/java";

        Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "windup_" + RandomStringUtils.randomAlphanumeric(6));
        FileUtils.deleteDirectory(outputPath.toFile());
        Files.createDirectories(outputPath);

        
        // Fill the graph with test data.
        GraphLifecycleListener initializer = new GraphLifecycleListener() {
            public void postOpen( GraphContext context ){
                ProjectModel pm = context.getFramed().addVertex(null, ProjectModel.class);
                pm.setName("Main Proejct");

                FileModel inputPath = context.getFramed().addVertex(null, FileModel.class);
                inputPath.setFilePath(inputDir);
                inputPath.setProjectModel(pm);
                pm.setRootFileModel(inputPath);

                FileModel fileModel = context.getFramed().addVertex(null, FileModel.class);
                fileModel.setFilePath(inputDir + "/HintsClassificationsTest.java");
                fileModel.setProjectModel(pm);

                pm.addFileModel(inputPath);
                pm.addFileModel(fileModel);
                fileModel = context.getFramed().addVertex(null, FileModel.class);
                fileModel.setFilePath("src/test/java/org/jboss/windup/rules/java/JavaClassTest.java");
                fileModel.setProjectModel(pm);
                pm.addFileModel(fileModel);
            }
            
            public void preShutdown(GraphContext context){
            }
        };

        final WindupProcessorConfig processorConfig = new WindupProcessorConfig().setOutputDirectory(outputPath);
        processorConfig.setGraphListener( initializer );
                
        final WindupConfigurationModel config = GraphService.getConfigurationModel(context);
        config.setInputPath(inputDir);
        config.setSourceMode(true);
        config.setOutputPath(outputPath.toString());
        config.setScanJavaPackageList(Collections.singletonList(""));

        try
        {
            // TODO: Consolidate the config - e.g. the outputPath is now set at 2 places.
            processor.execute(processorConfig);
        }
        catch (Exception e)
        {
            if (!e.getMessage().contains("CreateMainApplicationReport"))
                throw e;
        }

        GraphService<TypeReferenceModel> typeRefService = new GraphService<>(context, TypeReferenceModel.class);
        Iterable<TypeReferenceModel> typeReferences = typeRefService.findAll();
        Assert.assertTrue(typeReferences.iterator().hasNext());

        Assert.assertEquals(3, provider.getFirstRuleMatchCount());
        Assert.assertEquals(1, provider.getSecondRuleMatchCount());
    }

    @Singleton
    public static class TestJavaClassTestRuleProvider extends WindupRuleProvider
    {
        private int firstRuleMatchCount = 0;
        private int secondRuleMatchCount = 0;

        @Override
        public RulePhase getPhase()
        {
            return RulePhase.MIGRATION_RULES;
        }

        public int getFirstRuleMatchCount()
        {
            return firstRuleMatchCount;
        }

        public void setFirstRuleMatchCount(int firstRuleMatchCount)
        {
            this.firstRuleMatchCount = firstRuleMatchCount;
        }

        public int getSecondRuleMatchCount()
        {
            return secondRuleMatchCount;
        }

        public void setSecondRuleMatchCount(int secondRuleMatchCount)
        {
            this.secondRuleMatchCount = secondRuleMatchCount;
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
            return ConfigurationBuilder.begin()
                        
                        .addRule()
                        .when(JavaClass.references("org.jboss.forge.furnace.*").inFile(".*").at(TypeReferenceLocation.IMPORT))
                        .perform(new AbstractIterationOperation<TypeReferenceModel>()
                                                {
                                        @Override
                                        public void perform(GraphRewrite event, EvaluationContext context, TypeReferenceModel payload)
                                        {
                                            firstRuleMatchCount++;
                                        }
                                    }
                        )
                        .addRule()
                        .when(JavaClass.references("org.jboss.forge.furnace.*").inFile(".*JavaClassTest.*").at(TypeReferenceLocation.IMPORT))
                        .perform(new AbstractIterationOperation<TypeReferenceModel>()
                                                {
                                        @Override
                                        public void perform(GraphRewrite event, EvaluationContext context, TypeReferenceModel payload)
                                        {
                                            secondRuleMatchCount++;
                                        }
                                    }
                        );
        }
        // @formatter:on

    }

}
