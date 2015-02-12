package org.jboss.windup.rules.xml.handlers.unit;

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
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.jboss.windup.util.exception.WindupException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(Arquillian.class)
public class XmlFileHandlerTest
{

    private static final String XML_FILE = "src/test/resources/unit/xmlfile.windup.xml";

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java", version = "2.0.0-SNAPSHOT"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-xml"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config-xml", version = "2.0.0-SNAPSHOT"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClass(XmlFileHandlerTest.class)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-xml"),
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config-xml"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );

        return archive;
    }

    @Inject
    private Furnace furnace;

    @Test
    public void testXmlFileCondition() throws Exception
    {
        ParserContext parser = new ParserContext(furnace);
        File fXmlFile = new File(XML_FILE);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> xmlFileList = $(doc).children("xmlfile").get();

        Element firstXmlFile = xmlFileList.get(0);
        XmlFile xmlFile = parser.<XmlFile> processElement(firstXmlFile);
        // verify xmlfile
        Assert.assertEquals(null, xmlFile.getInputVariablesName());
        Assert.assertEquals("public", xmlFile.getPublicId());
        Assert.assertEquals(".*", xmlFile.getInFilePattern().getPattern());
        Assert.assertEquals("/abc:project", xmlFile.getXpathString());

        Element secondXmlFile = xmlFileList.get(1);
        xmlFile = parser.<XmlFile> processElement(secondXmlFile);
        // verify xmlfile
        Assert.assertEquals(null, xmlFile.getInputVariablesName());
        Assert.assertEquals("public", xmlFile.getPublicId());
        Assert.assertEquals(null, xmlFile.getInFilePattern());
        Assert.assertEquals(null, xmlFile.getXpathString());
    }

    @Test(expected = WindupException.class)
    public void testXmlFileWithoutPublidIdAndXpath() throws Exception
    {
        ParserContext parser = new ParserContext(furnace);
        File fXmlFile = new File(XML_FILE);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> xmlFileList = $(doc).children("xmlfile").get();

        Element thirdXmlFile = xmlFileList.get(2);
        XmlFile xmlFile = parser.<XmlFile> processElement(thirdXmlFile);
    }
}
