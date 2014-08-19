package org.jboss.windup.rules.xml;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

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
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.engine.WindupProcessor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.config.Classification;
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
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-xml"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClass(XMLHintsClassificationsTestRuleProvider.class)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-xml"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );

        return archive;
    }

    @Inject
    private XMLHintsClassificationsTestRuleProvider provider;

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContext context;

    @Test
    public void testIterationVariableResolving()
    {
        FileModel inputPath = context.getFramed().addVertex(null, FileModel.class);
        inputPath.setFilePath("src/test/resources/");

        WindupConfigurationModel config = GraphService.getConfigurationModel(context);
        config.setInputPath(inputPath);
        config.setSourceMode(true);

        try
        {
            processor.execute();
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
        Assert.assertEquals(1, classifications.size());
    }

    @Singleton
    public static class XMLHintsClassificationsTestRuleProvider extends WindupRuleProvider
    {
        private Set<FileModel> xmlFiles = new HashSet<>();

        @Override
        public RulePhase getPhase()
        {
            return RulePhase.MIGRATION_RULES;
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            AbstractIterationOperation<FileModel> addTypeRefToList = new AbstractIterationOperation<FileModel>()
            {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context, FileModel payload)
                {
                    xmlFiles.add(payload);
                }
            };
            
            return ConfigurationBuilder.begin()
                        
                        .addRule()
                        .when(XmlFile.matchesXpath("/project"))
                        .perform(Iteration.over()
                                    .perform(Classification.as("Maven POM File")
                                                .with(Link.to("Apache Maven POM Reference", "http://maven.apache.org/pom.html")).withEffort(0)
                                                .and(addTypeRefToList)
                                    ).endIteration()
                        );
        }
        // @formatter:on

        public Set<FileModel> getXmlFileMatches()
        {
            return xmlFiles;
        }
    }

}
