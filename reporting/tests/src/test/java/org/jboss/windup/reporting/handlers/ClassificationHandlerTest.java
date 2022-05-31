package org.jboss.windup.reporting.handlers;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.Furnace;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.reporting.config.classification.Classification;
import org.jboss.windup.util.exception.WindupException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.joox.JOOX.$;

@RunWith(Arquillian.class)
public class ClassificationHandlerTest {

    private static final String CLASSIFICATION_XML_WINDUP_FILE = "src/test/resources/handler/classification.windup.xml";
    private static final String CLASSIFICATION_XML_RHAMT_FILE = "src/test/resources/handler/classification.rhamt.xml";
    private static final String CLASSIFICATION_XML_MTA_FILE = "src/test/resources/handler/classification.mta.xml";
    @Inject
    private Furnace furnace;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-xml"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")})
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Test
    public void testWindupClassificationParsing() throws Exception {
        File fXmlFile = new File(CLASSIFICATION_XML_WINDUP_FILE);
        testClassificationParsing(fXmlFile);
    }

    @Test
    public void testRhamtClassificationParsing() throws Exception {
        File fXmlFile = new File(CLASSIFICATION_XML_RHAMT_FILE);
        testClassificationParsing(fXmlFile);
    }

    @Test
    public void testMtaClassificationParsing() throws Exception {
        File fXmlFile = new File(CLASSIFICATION_XML_MTA_FILE);
        testClassificationParsing(fXmlFile);
    }

    public void testClassificationParsing(File fXmlFile) throws Exception {
        RuleLoaderContext loaderContext = new RuleLoaderContext(Collections.singleton(fXmlFile.toPath()), null);
        ParserContext parser = new ParserContext(furnace, loaderContext);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> classificationList = $(doc).children("classification").get();
        Element firstClassification = classificationList.get(0);
        Classification classification = parser.<Classification>processElement(firstClassification);

        Assert.assertNull(classification.getIssueCategory());
        Assert.assertEquals("testVariable", classification.getVariableName());
        Assert.assertEquals(5, classification.getEffort());
        Assert.assertEquals("test message", classification.getClassificationPattern().toString());
        Assert.assertEquals("simple description", classification.getDescriptionPattern().toString());
        Assert.assertEquals(1, classification.getLinks().size());
        List<Link> links = classification.getLinks();
        Assert.assertEquals("someUrl", links.get(0).getLink());
        Assert.assertEquals("someDescription", links.get(0).getTitle());

        Element secondClassification = classificationList.get(1);
        classification = parser.<Classification>processElement(secondClassification);
        Assert.assertEquals(null, classification.getVariableName());
        Assert.assertEquals(IssueCategoryRegistry.OPTIONAL, classification.getIssueCategory().getCategoryID());
        Assert.assertEquals(0, classification.getEffort());
        Assert.assertEquals("test-message", classification.getClassificationPattern().toString());
        Assert.assertEquals(null, classification.getDescriptionPattern());
        Assert.assertEquals(3, classification.getLinks().size());
        links = classification.getLinks();
        Assert.assertEquals("url1", links.get(0).getLink());
        Assert.assertEquals("url2", links.get(1).getLink());
        Assert.assertEquals("url3", links.get(2).getLink());
        Assert.assertEquals("description1", links.get(0).getTitle());
        Assert.assertEquals("description2", links.get(1).getTitle());
        Assert.assertEquals("description3", links.get(2).getTitle());

    }

    @Test(expected = WindupException.class)
    public void testWindupClassificationWithoutMessage() throws Exception {
        File fXmlFile = new File(CLASSIFICATION_XML_WINDUP_FILE);
        testClassificationWithoutMessage(fXmlFile);
    }

    @Test(expected = WindupException.class)
    public void testRhamtClassificationWithoutMessage() throws Exception {
        File fXmlFile = new File(CLASSIFICATION_XML_RHAMT_FILE);
        testClassificationWithoutMessage(fXmlFile);
    }

    @Test(expected = WindupException.class)
    public void testMtaClassificationWithoutMessage() throws Exception {
        File fXmlFile = new File(CLASSIFICATION_XML_MTA_FILE);
        testClassificationWithoutMessage(fXmlFile);
    }

    public void testClassificationWithoutMessage(File fXmlFile) throws Exception {
        RuleLoaderContext loaderContext = new RuleLoaderContext(Collections.singleton(fXmlFile.toPath()), null);
        ParserContext parser = new ParserContext(furnace, loaderContext);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> classificationList = $(doc).children("classification").get();
        Element firstClassification = classificationList.get(2);
        parser.<Classification>processElement(firstClassification);
    }

    @Test(expected = WindupException.class)
    public void testWindupClassificationWithWrongEffort() throws Exception {
        File fXmlFile = new File(CLASSIFICATION_XML_WINDUP_FILE);
        testClassificationWithWrongEffort(fXmlFile);
    }

    @Test(expected = WindupException.class)
    public void testRhamtClassificationWithWrongEffort() throws Exception {
        File fXmlFile = new File(CLASSIFICATION_XML_RHAMT_FILE);
        testClassificationWithWrongEffort(fXmlFile);
    }

    @Test(expected = WindupException.class)
    public void testMtaClassificationWithWrongEffort() throws Exception {
        File fXmlFile = new File(CLASSIFICATION_XML_MTA_FILE);
        testClassificationWithWrongEffort(fXmlFile);
    }

    public void testClassificationWithWrongEffort(File fXmlFile) throws Exception {
        RuleLoaderContext loaderContext = new RuleLoaderContext(Collections.singleton(fXmlFile.toPath()), null);
        ParserContext parser = new ParserContext(furnace, loaderContext);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> classificationList = $(doc).children("classification").get();
        Element firstClassification = classificationList.get(3);
        parser.<Classification>processElement(firstClassification);
    }
}