package org.jboss.windup.rules.xml;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
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
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.engine.WindupProcessor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.FileLocationModel;
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.jboss.windup.rules.apps.xml.model.XsltTransformationModel;
import org.jboss.windup.rules.apps.xml.operation.xslt.XSLTTransformation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

@RunWith(Arquillian.class)
public class XMLTransformationTest
{
    
    private static final String SIMPLE_XSLT_XSL = "simpleXSLT.xsl";
    private static final String XSLT_EXTENSION = "-result.html";
    
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
                    .addClass(TestXMLTransformationRuleProvider.class)
                    .addAsResource("simpleXSLT.xsl")
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

        GraphService<XsltTransformationModel> transformationService = new GraphService<>(context, XsltTransformationModel.class);
        inputPath.setProjectModel(pm);
        pm.setRootFileModel(inputPath);
        
        Assert.assertFalse(transformationService.findAll().iterator().hasNext());
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

        Iterator<XsltTransformationModel> iterator = transformationService.findAll().iterator();
        Assert.assertTrue(iterator.hasNext());
        XsltTransformationModel xsltTransformation = iterator.next();
        Assert.assertEquals(SIMPLE_XSLT_XSL,xsltTransformation.getSourceLocation());
        Assert.assertEquals(XSLT_EXTENSION,xsltTransformation.getExtension());
        int lineFound = 0;
        try(BufferedReader br = new BufferedReader(new FileReader(xsltTransformation.getResult()))) {
            String line = br.readLine();
            while (line != null) {
               if(line.contains("found GroupId")) {
                   lineFound++;
               }
               line=br.readLine();
            }
        }
        Assert.assertEquals(19,lineFound);
    }

    @Singleton
    public static class TestXMLTransformationRuleProvider extends WindupRuleProvider
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
            return ConfigurationBuilder
                        .begin()
                        .addRule()
                        .when(XmlFile.matchesXpath("/abc:project")
                                    .namespace("abc", "http://maven.apache.org/POM/4.0.0"))
                        .perform(XSLTTransformation.using(SIMPLE_XSLT_XSL).withExtension(XSLT_EXTENSION));
        }

        // @formatter:on

        public Set<FileLocationModel> getXmlFileMatches()
        {
            return xmlFiles;
        }
    }

}