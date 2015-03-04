package org.jboss.windup.rules.xml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.config.phase.PostMigrationRulesPhase;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.jboss.windup.rules.files.model.FileLocationModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * This tests a scenario that combines a parameter from an XML file with a Java class. This should match only one of the
 * two Java files in the source directory.
 */
@RunWith(Arquillian.class)
public class XmlAndJavaParameterizedTest
{
    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-base"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-xml"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:windup-rules-base"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:windup-rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:windup-rules-xml"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );

        return archive;
    }

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testXmlAndJavaSearchParams() throws IOException
    {
        try (GraphContext context = factory.create())
        {
            ProjectModel pm = context.getFramed().addVertex(null, ProjectModel.class);
            pm.setName("Main Project");
            FileModel inputPath = context.getFramed().addVertex(null, FileModel.class);
            inputPath.setFilePath("src/test/resources/parameterizationtests");

            Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "windup_"
                        + UUID.randomUUID().toString());
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            inputPath.setProjectModel(pm);
            pm.setRootFileModel(inputPath);

            Predicate<RuleProvider> predicate = new Predicate<RuleProvider>()
            {
                @Override
                public boolean accept(RuleProvider provider)
                {
                    return (provider.getMetadata().getPhase() != ReportGenerationPhase.class) &&
                                (provider.getMetadata().getPhase() != MigrationRulesPhase.class);
                }
            };
            WindupConfiguration windupConfiguration = new WindupConfiguration()
                        .setRuleProviderFilter(predicate)
                        .setGraphContext(context);
            windupConfiguration.setInputPath(Paths.get(inputPath.getFilePath()));
            windupConfiguration.setOutputDirectory(outputPath);
            processor.execute(windupConfiguration);

            GraphService<InlineHintModel> hintService = new GraphService<>(context, InlineHintModel.class);
            int count = 0;
            for (InlineHintModel model : hintService.findAll())
            {
                String text = model.getHint();
                System.out.println("Model: " + text + ", full: " + model);
                Assert.assertNotNull(text);
                Assert.assertEquals("Found value: ParameterizationSampleJavaClass", text);
                count++;
            }
            Assert.assertEquals(1, count);
        }
    }

    public static class TestParameterizedXmlRuleProvider extends AbstractRuleProvider
    {
        private Set<FileLocationModel> xmlFiles = new HashSet<>();

        public TestParameterizedXmlRuleProvider()
        {
            super(MetadataBuilder.forProvider(TestParameterizedXmlRuleProvider.class).setPhase(PostMigrationRulesPhase.class));
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            AbstractIterationOperation<FileLocationModel> addTypeRefToList = new AbstractIterationOperation<FileLocationModel>()
            {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context, FileLocationModel payload)
                {
                    xmlFiles.add(payload);
                }
            };

            return ConfigurationBuilder
                        .begin()
                        .addRule()
                        .when(XmlFile.matchesXpath(
                                        "/javaclasses" + 
                                        "/javaclass[windup:matches(text(), '{classname}')]"
                                    ).as("javaclassnamesfromxml")
                              .and(
                                  JavaClass.references("{classname}")
                                           .as("javaClasses")
                              )
                        )
                        .perform(
                            Iteration.over("javaClasses").perform(
                                    Hint.withText("Found value: {classname}").withEffort(2)
                                     .and(addTypeRefToList)
                             ).endIteration()
                         );
        }
        // @formatter:on

        public Set<FileLocationModel> getXmlFileMatches()
        {
            return xmlFiles;
        }
    }

}
