package org.jboss.windup.rules.apps.xml.operation.xslt;

import static org.joox.JOOX.$;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.util.Iterators;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.rulefilters.NotPredicate;
import org.jboss.windup.exec.rulefilters.RuleProviderPhasePredicate;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.util.exception.WindupException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(Arquillian.class)
public class XSLTTransformationHandlerTest {

    private static final String XSLT_WINDUP_FILE = "src/test/resources/unit/xslt.windup.xml";
    private static final String XSLT_RHAMT_FILE = "src/test/resources/unit/xslt.rhamt.xml";
    private static final String XSLT_MTA_FILE = "src/test/resources/unit/xslt.mta.xml";

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-xml"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-xml"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Inject
    private GraphContextFactory factory;

    @Inject
    private WindupProcessor processor;

    @Inject
    private Furnace furnace;

    @Inject
    private Addon addon;

    @Test
    public void testWindupXSLTOperation() throws Exception {
        File fXmlFile = new File(XSLT_WINDUP_FILE);
        testXSLTOperation(fXmlFile);
    }

    @Test
    public void testRhamtXSLTOperation() throws Exception {
        File fXmlFile = new File(XSLT_RHAMT_FILE);
        testXSLTOperation(fXmlFile);
    }

    @Test
    public void testMtaXSLTOperation() throws Exception {
        File fXmlFile = new File(XSLT_MTA_FILE);
        testXSLTOperation(fXmlFile);
    }

    public void testXSLTOperation(File fXmlFile) throws Exception {
        RuleLoaderContext loaderContext = new RuleLoaderContext(Collections.singleton(fXmlFile.toPath()), null);
        ParserContext parser = new ParserContext(furnace, loaderContext);
        parser.setAddonContainingInputXML(addon);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> xsltList = $(doc).children("xslt").get();

        Element firstXslt = xsltList.get(0);
        XSLTTransformation xsltOperation = parser.<XSLTTransformation>processElement(firstXslt);
        // verify xsltOperation
        Assert.assertEquals("XSLT Tranformed Output", xsltOperation.getDescription());
        Assert.assertEquals("-test-result.html", xsltOperation.getExtension());
        Assert.assertEquals("testVariable_instance", xsltOperation.getVariableName());
        Assert.assertEquals("simpleXSLT.xsl", xsltOperation.getTemplate());
        Assert.assertFalse(xsltOperation.isUseSaxon());

        Element secondXslt = xsltList.get(1);
        xsltOperation = parser.<XSLTTransformation>processElement(secondXslt);
        // verify xmlfile
        Assert.assertEquals("XSLT Tranformed Output", xsltOperation.getDescription());
        Assert.assertEquals("-test-result.html", xsltOperation.getExtension());
        Assert.assertEquals(null, xsltOperation.getVariableName());
        Assert.assertEquals("simpleXSLT.xsl", xsltOperation.getTemplate());
        Assert.assertFalse(xsltOperation.isUseSaxon());

        Element fifthXslt = xsltList.get(4);
        xsltOperation = parser.<XSLTTransformation>processElement(fifthXslt);
        Assert.assertTrue(xsltOperation.isUseSaxon());
    }

    @Test
    public void testWithIncludedStylesheet() throws IOException {
        String inputPath = "src/test/resources/xslttransform";
        Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "Windup", "windup_"
                + UUID.randomUUID().toString());

        try (GraphContext context = factory.create(true)) {
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            WindupConfiguration windupConfiguration = new WindupConfiguration()
                    .setRuleProviderFilter(new NotPredicate(
                            new RuleProviderPhasePredicate(ReportGenerationPhase.class)
                    ))
                    .setGraphContext(context);
            windupConfiguration.addInputPath(Paths.get(inputPath));
            windupConfiguration.setOutputDirectory(outputPath);
            windupConfiguration.addDefaultUserRulesDirectory(Paths.get(inputPath));
            processor.execute(windupConfiguration);

            GraphService<ClassificationModel> classificationService = new GraphService<>(context, ClassificationModel.class);

            List<ClassificationModel> classificationModels = Iterators.asList(classificationService.findAll());

            Assert.assertEquals(1, classificationModels.size());

            Assert.assertTrue(classificationModels.get(0).getClassification().contains("XSLT Transformed Output"));

            Path transformedFile = outputPath.resolve("reports").resolve("xslttransform").resolve("transformedxml").resolve("sample-xml-test-result.html");
            Assert.assertTrue(Files.isRegularFile(transformedFile));
            Assert.assertTrue(Files.size(transformedFile) > 0);
        }
    }

    @Test(expected = WindupException.class)
    public void testWindupXSLTWithoutExtension() throws Exception {
        File fXmlFile = new File(XSLT_WINDUP_FILE);
        testXSLTWithoutExtension(fXmlFile);
    }

    @Test(expected = WindupException.class)
    public void testRhamtXSLTWithoutExtension() throws Exception {
        File fXmlFile = new File(XSLT_RHAMT_FILE);
        testXSLTWithoutExtension(fXmlFile);
    }

    @Test(expected = WindupException.class)
    public void testMtaXSLTWithoutExtension() throws Exception {
        File fXmlFile = new File(XSLT_MTA_FILE);
        testXSLTWithoutExtension(fXmlFile);
    }

    public void testXSLTWithoutExtension(File fXmlFile) throws Exception {
        RuleLoaderContext loaderContext = new RuleLoaderContext(Collections.singleton(fXmlFile.toPath()), null);
        ParserContext parser = new ParserContext(furnace, loaderContext);
        parser.setAddonContainingInputXML(addon);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> xsltList = $(doc).children("xslt").get();

        Element firstXslt = xsltList.get(2);
        XSLTTransformation xsltOperation = parser.<XSLTTransformation>processElement(firstXslt);
    }

    @Test(expected = WindupException.class)
    public void testWindupXSLTWithoutTemplate() throws Exception {
        File fXmlFile = new File(XSLT_WINDUP_FILE);
        testXSLTWithoutTemplate(fXmlFile);
    }

    @Test(expected = WindupException.class)
    public void testRhamtXSLTWithoutTemplate() throws Exception {
        File fXmlFile = new File(XSLT_RHAMT_FILE);
        testXSLTWithoutTemplate(fXmlFile);
    }

    @Test(expected = WindupException.class)
    public void testMtaXSLTWithoutTemplate() throws Exception {
        File fXmlFile = new File(XSLT_MTA_FILE);
        testXSLTWithoutTemplate(fXmlFile);
    }

    public void testXSLTWithoutTemplate(File fXmlFile) throws Exception {
        RuleLoaderContext loaderContext = new RuleLoaderContext(Collections.singleton(fXmlFile.toPath()), null);
        ParserContext parser = new ParserContext(furnace, loaderContext);
        parser.setAddonContainingInputXML(addon);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> xsltList = $(doc).children("xslt").get();

        Element firstXslt = xsltList.get(3);
        XSLTTransformation xsltOperation = parser.<XSLTTransformation>processElement(firstXslt);
    }
}