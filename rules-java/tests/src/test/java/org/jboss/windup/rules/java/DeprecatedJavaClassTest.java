package org.jboss.windup.rules.java;

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
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.metadata.TechnologyReference;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.engine.predicates.RuleProviderWithDependenciesPredicate;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.configuration.options.TargetOption;
import org.jboss.windup.exec.configuration.options.UserRulesDirectoryOption;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.java.scan.ast.AnalyzeJavaFilesRuleProvider;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.testutil.basics.WindupTestUtilMethods;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.logging.Logger;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class DeprecatedJavaClassTest
{
    @Inject
    JavaClassTestRuleProvider provider;
    @Inject
    private WindupProcessor processor;
    @Inject
    private GraphContextFactory factory;

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

    @Test
    public void testJavaClassConditionWithDeprecatedMethod() throws IOException, InstantiationException, IllegalAccessException
    {
        try (GraphContext context = factory.create(WindupTestUtilMethods.getTempDirectoryForGraph(), true))
        {
            final String inputDir = "src/test/resources/org/jboss/windup/rules/java";

            final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "windup_" + RandomStringUtils.randomAlphanumeric(6));
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            ProjectModel pm = context.getFramed().addFramedVertex(ProjectModel.class);
            pm.setName("Main Project");

            FileModel inputPathFrame = context.getFramed().addFramedVertex(FileModel.class);
            inputPathFrame.setFilePath(inputDir);
            pm.addFileModel(inputPathFrame);
            pm.setRootFileModel(inputPathFrame);

            FileModel fileModel = context.getFramed().addFramedVertex(FileModel.class);
            fileModel.setFilePath(inputDir + "/JavaClassTestFile3.java");
            pm.addFileModel(fileModel);

            context.commit();

            final WindupConfiguration processorConfig = new WindupConfiguration();
            processorConfig.setRuleProviderFilter(new RuleProviderWithDependenciesPredicate(JavaClassTestRuleProvider.class));
            processorConfig.setGraphContext(context);
            processorConfig.addInputPath(Paths.get(inputDir));
            processorConfig.setOutputDirectory(outputPath);
            processorConfig.setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""));
            processorConfig.setOptionValue(SourceModeOption.NAME, true);
            processorConfig.setOptionValue(TargetOption.NAME, Collections.singletonList("jdk"));
            processorConfig.setOptionValue(UserRulesDirectoryOption.NAME, Collections.singletonList(new File("src/test/resources/org/jboss/windup/rules/technologyMetadata")));

            processor.execute(processorConfig);

            GraphService<JavaTypeReferenceModel> typeRefService = new GraphService<>(context, JavaTypeReferenceModel.class);
            Iterable<JavaTypeReferenceModel> typeReferences = typeRefService.findAll();
            assertTrue(typeReferences.iterator().hasNext());

            assertEquals(2, provider.getFirstRuleMatchCount());
        }
    }

    @Singleton
    public static class JavaClassTestRuleProvider extends AbstractRuleProvider
    {
        private static final Logger log = Logger.getLogger(RuleSubset.class.getName());

        private int firstRuleMatchCount = 0;

        public JavaClassTestRuleProvider()
        {
            super(MetadataBuilder.forProvider(JavaClassTestRuleProvider.class)
                    .addExecuteAfter(AnalyzeJavaFilesRuleProvider.class)
                    .addTargetTechnology(new TechnologyReference("jdk")));
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
        {
            return ConfigurationBuilder.begin()
            .addRule().when(
                JavaClass.references("java.awt.Font.getPeer({*})").inType("{*}").at(TypeReferenceLocation.METHOD_CALL).as("getPeerMatch")
            ).perform(
                Iteration.over("getPeerMatch").perform(new AbstractIterationOperation<JavaTypeReferenceModel>()
                {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload)
                    {
                        firstRuleMatchCount++;
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

    }
}
