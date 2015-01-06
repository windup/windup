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
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.model.FileLocationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

@RunWith(Arquillian.class)
public class XmlFileParameterizedTest
{
    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                /*
                 * FIXME: Convert the XML addon to complex layout with separate tests/ module to remove this hard-coded version
                 */
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java", version = "2.0.0-SNAPSHOT"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-xml"),
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
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-xml"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );

        return archive;
    }

    @Inject
    private TestParameterizedXmlRuleProvider provider;

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testXmlParams() throws IOException
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
                    return (provider.getPhase() != RulePhase.REPORT_GENERATION) &&
                                (provider.getPhase() != RulePhase.MIGRATION_RULES);
                }
            };
            WindupConfiguration windupConfiguration = new WindupConfiguration()
                        .setRuleProviderFilter(predicate)
                        .setGraphContext(context);
            windupConfiguration.setInputPath(Paths.get(inputPath.getFilePath()));
            windupConfiguration.setOutputDirectory(outputPath);
            processor.execute(windupConfiguration);

            GraphService<InlineHintModel> hintService = new GraphService<>(context, InlineHintModel.class);
            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean foundWrongValue = false;
            boolean found5 = false;
            boolean found6 = false;
            boolean found7 = false;
            boolean found8 = false;
            boolean found9 = false;
            boolean found10 = false;
            for (InlineHintModel model : hintService.findAll())
            {
                String text = model.getHint();
                Assert.assertNotNull(text);
                if (text.equals("Found value: 1"))
                {
                    found1 = true;
                }
                else if (text.equals("Found value: 2"))
                {
                    found2 = true;
                }
                else if (text.equals("Found value: 3"))
                {
                    found3 = true;
                }
                else if (text.equals("Found value: 4"))
                {
                    found4 = true;
                }
                else if (text.equals("Found value: wrongvalue"))
                {
                    foundWrongValue = true;
                }
                else if (text.equals("Found value: 5"))
                {
                    found5 = true;
                }
                else if (text.equals("Found value: 6"))
                {
                    found6 = true;
                }
                else if (text.equals("Found value: 7"))
                {
                    found7 = true;
                }
                else if (text.equals("Found value: 8"))
                {
                    found8 = true;
                }
                else if (text.equals("Found value: 9"))
                {
                    found9 = true;
                }
                else if (text.equals("Found value: 10"))
                {
                    found10 = true;
                }
                System.out.println("Model: " + model.getHint() + ", full: " + model);
            }
            Assert.assertTrue(found1);
            Assert.assertTrue(found2);
            Assert.assertTrue(found3);
            Assert.assertFalse(found4);
            Assert.assertFalse(foundWrongValue);
            Assert.assertTrue(found5);
            Assert.assertTrue(found6);
            Assert.assertTrue(found7);
            Assert.assertFalse(found8);
            Assert.assertTrue(found9);
            Assert.assertTrue(found10);
        }
    }

    public static class TestParameterizedXmlRuleProvider extends WindupRuleProvider
    {
        private Set<FileLocationModel> xmlFiles = new HashSet<>();

        @Override
        public RulePhase getPhase()
        {
            return RulePhase.POST_MIGRATION_RULES;
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
                        .when(XmlFile.matchesXpath("/root/row[windup:startFrame(0) and windup:evaluate(0, windup:matches(0, index/text(), '{index}'))]/@indexAtt[windup:startFrame(1) and windup:evaluate(1, windup:matches(1, self::node(), '{index}'))]/self::node()[windup:persist(2, .)]"))
                        .perform(Hint.withText("Found value: {index}").withEffort(2)
                                     .and(addTypeRefToList));
        }
        // @formatter:on

        public Set<FileLocationModel> getXmlFileMatches()
        {
            return xmlFiles;
        }
    }

}
