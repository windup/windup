package org.jboss.windup.reporting.handlers;

import static org.joox.JOOX.$;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.reporting.config.classification.Classification;
import org.jboss.windup.util.exception.WindupException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(Arquillian.class)
public class ClassificationHandlerTest
{

    private static final String CLASSIFICATION_XML_FILE = "src/test/resources/handler/classification.windup.xml";

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java", version = "2.0.0-SNAPSHOT"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config-xml", version = "2.0.0-SNAPSHOT"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi") })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap
                    .create(ForgeArchive.class)
                    .addBeansXML()
                    .addClass(HintHandlerTest.class)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config-xml"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"));
        return archive;
    }

    @Inject
    private Furnace furnace;

    @Test
    public void testJavaClassCondition() throws Exception
    {
        ParserContext parser = new ParserContext(furnace);
        File fXmlFile = new File(CLASSIFICATION_XML_FILE);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> classificationList = $(doc).children("classification").get();
        Element firstClassification = classificationList.get(0);
        Classification classification = parser.<Classification> processElement(firstClassification);

        Assert.assertEquals("testVariable", classification.getVariableName());
        Assert.assertEquals(5, classification.getEffort());
        Assert.assertEquals("test message", classification.getClassificationPattern().toString());
        Assert.assertEquals("simple description", classification.getDescriptionPattern().toString());
        Assert.assertEquals(1, classification.getLinks().size());
        List<Link> links = classification.getLinks();
        Assert.assertEquals("someUrl", links.get(0).getLink());
        Assert.assertEquals("someDescription", links.get(0).getDescription());

        Element secondClassification = classificationList.get(1);
        classification = parser.<Classification> processElement(secondClassification);
        Assert.assertEquals(null, classification.getVariableName());
        Assert.assertEquals(0, classification.getEffort());
        Assert.assertEquals("test-message", classification.getClassificationPattern().toString());
        Assert.assertEquals(null, classification.getDescriptionPattern());
        Assert.assertEquals(3, classification.getLinks().size());
        links = classification.getLinks();
        Assert.assertEquals("url1", links.get(0).getLink());
        Assert.assertEquals("url2", links.get(1).getLink());
        Assert.assertEquals("url3", links.get(2).getLink());
        Assert.assertEquals("description1", links.get(0).getDescription());
        Assert.assertEquals("description2", links.get(1).getDescription());
        Assert.assertEquals("description3", links.get(2).getDescription());

    }

    @Test(expected = WindupException.class)
    public void testClassificationWithoutMessage() throws Exception
    {
        ParserContext parser = new ParserContext(furnace);
        File fXmlFile = new File(CLASSIFICATION_XML_FILE);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> classificationList = $(doc).children("classification").get();
        Element firstClassification = classificationList.get(2);
        Classification classification = parser.<Classification> processElement(firstClassification);

    }

    @Test(expected = WindupException.class)
    public void testClassificationWithWrongEffort() throws Exception
    {
        ParserContext parser = new ParserContext(furnace);
        File fXmlFile = new File(CLASSIFICATION_XML_FILE);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> classificationList = $(doc).children("classification").get();
        Element firstClassification = classificationList.get(3);
        Classification classification = parser.<Classification> processElement(firstClassification);

    }
}