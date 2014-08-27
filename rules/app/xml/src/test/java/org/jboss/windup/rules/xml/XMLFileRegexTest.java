package org.jboss.windup.rules.xml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
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
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.engine.WindupProcessor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.FileLocationModel;
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

@RunWith(Arquillian.class)
public class XMLFileRegexTest
{
    
    private static final String EXAMPLE_EJB_JAR_2_XML = "example-ejb-jar.2.xml";
    
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
                    .addClass(TestXMLFileRegexRuleProvider.class)
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
    private TestXMLFileRegexRuleProvider provider;

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
                    return provider.getPhase() != RulePhase.REPORT_GENERATION;
                }
            };
            processor.execute(predicate);
        }
        catch (Exception e)
        {
            if (!e.getMessage().contains("CreateMainApplicationReport"))
                throw e;
        }

        // 1 out of 2 should match the file regex
        Assert.assertEquals(1, provider.getXmlFileMatches().size());
        FileModel matchedFile = provider.getXmlFileMatches().iterator().next().getFile();
        String fileName = matchedFile.getFileName();
        Assert.assertTrue(fileName.equals(EXAMPLE_EJB_JAR_2_XML));
    }

    @Singleton
    public static class TestXMLFileRegexRuleProvider extends WindupRuleProvider
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
            AbstractIterationOperation<FileLocationModel> addMatchedFile = new AbstractIterationOperation<FileLocationModel>()
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
                        .when(XmlFile.matchesXpath("/abc:ejb-jar").inFile(EXAMPLE_EJB_JAR_2_XML)
                                    .namespace("abc", "http://java.sun.com/xml/ns/javaee"))
                        .perform(addMatchedFile);
        }

        // @formatter:on

        public Set<FileLocationModel> getXmlFileMatches()
        {
            return xmlFiles;
        }
    }

}