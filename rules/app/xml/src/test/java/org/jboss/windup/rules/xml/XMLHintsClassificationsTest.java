package org.jboss.windup.rules.xml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.engine.WindupProcessor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.engine.WindupProcessor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.config.Classification;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.FileLocationModel;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

@RunWith(Arquillian.class)
public class XMLHintsClassificationsTest
{
    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java", version = "2.0.0-SNAPSHOT"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-xml"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:java-decompiler", version = "2.0.0-SNAPSHOT"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClass(TestXMLHintsClassificationsRuleProvider.class)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-xml"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:java-decompiler"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );

        return archive;
    }

    @Inject
    private TestXMLHintsClassificationsRuleProvider provider;

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContext context;

    @Test
    public void testIterationVariableResolving() throws IOException
    {
        ProjectModel pm = context.getFramed().addVertex(null, ProjectModel.class);
        pm.setName("Main Project");
        FileModel inputPath = context.getFramed().addVertex(null, FileModel.class);
        inputPath.setFilePath("src/test/resources/");

        Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "windup_" + UUID.randomUUID().toString());
        FileUtils.deleteDirectory(outputPath.toFile());
        Files.createDirectories(outputPath);

        WindupConfigurationModel config = GraphService.getConfigurationModel(context);
        config.setInputPath(inputPath);
        config.setSourceMode(true);
        config.setOutputPath(outputPath.toString());

        inputPath.setProjectModel(pm);
        pm.setRootFileModel(inputPath);

        try
        {
            Predicate<WindupRuleProvider> predicate = new Predicate<WindupRuleProvider>()
            {
                @Override
                public boolean accept(WindupRuleProvider provider)
                {
                    return (provider.getPhase() != RulePhase.REPORT_GENERATION) && 
                                (provider.getPhase() != RulePhase.MIGRATION_RULES);
                }
            };
            processor.execute(predicate);
        }
        catch (Exception e)
        {
            if (!e.getMessage().contains("CreateMainApplicationReport"))
                throw e;
        }

        GraphService<InlineHintModel> hintService = new GraphService<>(context, InlineHintModel.class);
        GraphService<ClassificationModel> classificationService = new GraphService<>(context, ClassificationModel.class);

        Assert.assertEquals(2, provider.getXmlFileMatches().size());
        List<InlineHintModel> hints = Iterators.asList(hintService.findAll());
        Assert.assertEquals(2, hints.size());
        List<ClassificationModel> classifications = Iterators.asList(classificationService.findAll());
        for (ClassificationModel model : classifications) {
            String classification = model.getClassification();
            String string = classification.toString();
        }
        Assert.assertEquals(1, classifications.size());
    }

    @Singleton
    public static class TestXMLHintsClassificationsRuleProvider extends WindupRuleProvider
    {
        private Set<FileLocationModel> xmlFiles = new HashSet<>();

        @Override
        public RulePhase getPhase()
        {
            return RulePhase.MIGRATION_RULES;
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
                        .when(XmlFile.matchesXpath("/abc:ejb-jar")
                                    .namespace("abc", "http://java.sun.com/xml/ns/javaee"))
                        .perform(Classification.as("Maven POM File")
                                               .with(Link.to("Apache Maven POM Reference",
                                                            "http://maven.apache.org/pom.html")).withEffort(0)
                                               .and(Hint.withText("simple text").withEffort(2))
                                               .and(addTypeRefToList));
        }

        // @formatter:on

        public Set<FileLocationModel> getXmlFileMatches()
        {
            return xmlFiles;
        }
    }

}
