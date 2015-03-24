package org.jboss.windup.rules.xml;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.phase.ReportRenderingPhase;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.rulefilters.NotPredicate;
import org.jboss.windup.exec.rulefilters.RuleProviderPhasePredicate;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.xml.model.XsltTransformationModel;
import org.jboss.windup.rules.apps.xml.service.XsltTransformationService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class XMLTransformationXMLRulesTest
{

    private static final String SIMPLE_XSLT_XSL = "simpleXSLT.xsl";
    private static final String XSLT_EXTENSION = "-test-result.html";

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-xml"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsResource("simpleXSLT.xsl")
                    .addAsResource("simpleRule.windup.xml")
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
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
    public void testXSLTTransformation() throws IOException
    {
        try (GraphContext context = factory.create())
        {
            Path inputPath = Paths.get("src/test/resources/");

            Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "windup_"
                        + UUID.randomUUID().toString());
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            GraphService<XsltTransformationModel> transformationService = new GraphService<>(context,
                        XsltTransformationModel.class);

            Assert.assertFalse(transformationService.findAll().iterator().hasNext());

            WindupConfiguration windupConfiguration = new WindupConfiguration()
                        .setRuleProviderFilter(
                                    new NotPredicate(new RuleProviderPhasePredicate(ReportGenerationPhase.class, ReportRenderingPhase.class)))
                        .setGraphContext(context);
            windupConfiguration.setInputPath(inputPath);
            windupConfiguration.setOutputDirectory(outputPath);
            processor.execute(windupConfiguration);

            Iterator<XsltTransformationModel> iterator = transformationService.findAll().iterator();
            Assert.assertTrue(iterator.hasNext());
            XsltTransformationModel xsltTransformation = iterator.next();
            Assert.assertEquals(SIMPLE_XSLT_XSL, xsltTransformation.getSourceLocation());
            Assert.assertEquals(XSLT_EXTENSION, xsltTransformation.getExtension());
            XsltTransformationService xsltTransformationService = new XsltTransformationService(context);
            Path transformedPath = xsltTransformationService.getTransformedXSLTPath().resolve(
                        xsltTransformation.getResult());

            int lineFound = 0;
            try (BufferedReader br = new BufferedReader(new FileReader(transformedPath.toFile())))
            {
                String line = br.readLine();
                while (line != null)
                {
                    if (line.contains("found GroupId"))
                    {
                        lineFound++;
                    }
                    line = br.readLine();
                }
            }
            Assert.assertEquals(19, lineFound);
        }
    }
}