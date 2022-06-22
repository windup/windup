package org.jboss.windup.rules.xml.handlers.unit;

import static org.joox.JOOX.$;

import java.io.File;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.Furnace;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.jboss.windup.util.exception.WindupException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(Arquillian.class)
public class XmlFileHandlerTest {

    private static final String XML_FILE = "src/test/resources/unit/xmlfile.windup.xml";

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
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
    private Furnace furnace;

    @Test
    public void testXmlFileCondition() throws Exception {
        File fXmlFile = new File(XML_FILE);
        RuleLoaderContext loaderContext = new RuleLoaderContext(Collections.singleton(fXmlFile.toPath()), null);
        ParserContext parser = new ParserContext(furnace, loaderContext);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> xmlFileList = $(doc).children("xmlfile").get();

        Element firstXmlFile = xmlFileList.get(0);
        XmlFile xmlFile = parser.<XmlFile>processElement(firstXmlFile);
        // verify xmlfile
        Assert.assertEquals(null, xmlFile.getInputVariablesName());
        Assert.assertEquals("public", xmlFile.getPublicId());
        Assert.assertEquals(".*", xmlFile.getInFilePattern().getPattern());
        Assert.assertEquals("/abc:project", xmlFile.getXpathString());

        Element secondXmlFile = xmlFileList.get(1);
        xmlFile = parser.<XmlFile>processElement(secondXmlFile);
        // verify xmlfile
        Assert.assertEquals(null, xmlFile.getInputVariablesName());
        Assert.assertEquals("public", xmlFile.getPublicId());
        Assert.assertEquals(null, xmlFile.getInFilePattern());
        Assert.assertEquals(null, xmlFile.getXpathString());
    }

    @Test(expected = WindupException.class)
    public void testXmlFileWithoutPublidIdAndXpath() throws Exception {
        File fXmlFile = new File(XML_FILE);
        RuleLoaderContext loaderContext = new RuleLoaderContext(Collections.singleton(fXmlFile.toPath()), null);
        ParserContext parser = new ParserContext(furnace, loaderContext);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> xmlFileList = $(doc).children("xmlfile").get();

        Element thirdXmlFile = xmlFileList.get(2);
        XmlFile xmlFile = parser.<XmlFile>processElement(thirdXmlFile);
    }
}
