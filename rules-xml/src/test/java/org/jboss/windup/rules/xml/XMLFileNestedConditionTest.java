package org.jboss.windup.rules.xml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.util.Iterators;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.phase.MigrationRules;
import org.jboss.windup.config.phase.PostMigrationRules;
import org.jboss.windup.config.phase.ReportGeneration;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.config.classification.Classification;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.jboss.windup.rules.files.model.FileLocationModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

@RunWith(Arquillian.class)
public class XMLFileNestedConditionTest
{
    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                /*
                 * FIXME: Convert the XML addon to complex layout with separate tests/ module to remove this hard-coded version
                 */
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java", version = "2.0.0-SNAPSHOT"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-base"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-xml"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClass(TestXMLNestedXmlFileRuleProvider.class)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-base"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-xml"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );

        return archive;
    }

    @Inject
    private TestXMLNestedXmlFileRuleProvider provider;

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testNestedCondition() throws IOException
    {
        try (GraphContext context = factory.create())
        {
            ProjectModel pm = context.getFramed().addVertex(null, ProjectModel.class);
            pm.setName("Main Project");
            FileModel inputPath = context.getFramed().addVertex(null, FileModel.class);
            inputPath.setFilePath("src/test/resources/");

            Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "windup_"
                        + UUID.randomUUID().toString());
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            inputPath.setProjectModel(pm);
            pm.setRootFileModel(inputPath);

            Predicate<WindupRuleProvider> predicate = new Predicate<WindupRuleProvider>()
            {
                @Override
                public boolean accept(WindupRuleProvider provider)
                {
                    return (provider.getPhase() != ReportGeneration.class) &&
                                (provider.getPhase() != MigrationRules.class);
                }
            };
            WindupConfiguration windupConfiguration = new WindupConfiguration()
                        .setRuleProviderFilter(predicate)
                        .setGraphContext(context);
            windupConfiguration.setInputPath(Paths.get(inputPath.getFilePath()));
            windupConfiguration.setOutputDirectory(outputPath);
            processor.execute(windupConfiguration);

            GraphService<ClassificationModel> classificationService = new GraphService<>(context,
                        ClassificationModel.class);

            Assert.assertEquals(1, provider.getXmlFileMatches().size());
            List<ClassificationModel> classifications = Iterators.asList(classificationService.findAll());
            for (ClassificationModel model : classifications)
            {
                String classification = model.getClassification();
                String classificationString = classification.toString();
                Assert.assertEquals("Spring File", classificationString);
            }
            Assert.assertEquals(1, classifications.size());
            Iterator<FileModel> iterator = classifications.get(0).getFileModels().iterator();
            Assert.assertNotNull(iterator.next());
            Assert.assertNotNull(iterator.next());
            Assert.assertFalse(iterator.hasNext());
        }
    }

    @Singleton
    public static class TestXMLNestedXmlFileRuleProvider extends WindupRuleProvider
    {
        private Set<FileLocationModel> xmlFiles = new HashSet<>();

        @Override
        public Class<? extends RulePhase> getPhase()
        {
            return PostMigrationRules.class;
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
                        .when(XmlFile.matchesXpath("/abc:beans")
                                    .namespace("abc", "http://www.springframework.org/schema/beans").as("first"))
                        .perform(Classification.as("Spring File")
                                               .and(
                                                           Iteration.over("first")
                                                           .when(XmlFile.from(Iteration.singleVariableIterationName("first")).matchesXpath("//windupv1:file-gate").namespace("windupv1", "http://www.jboss.org/schema/windup"))
                                                           .perform(addTypeRefToList).endIteration()
                                                           )
                                );
        }
        // @formatter:on

        public Set<FileLocationModel> getXmlFileMatches()
        {
            return xmlFiles;
        }
    }

}