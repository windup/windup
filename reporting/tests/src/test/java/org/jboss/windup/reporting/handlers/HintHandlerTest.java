package org.jboss.windup.reporting.handlers;

import static org.joox.JOOX.$;

import java.io.File;
import java.util.List;
import java.util.Set;

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
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.reporting.model.Severity;
import org.jboss.windup.util.exception.WindupException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(Arquillian.class)
public class HintHandlerTest
{

    private static final String HINT_XML_FILE = "src/test/resources/handler/hint.windup.xml";

    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config-xml"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi") })
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap
                    .create(AddonArchive.class)
                    .addBeansXML();
    }

    @Inject
    private Furnace furnace;

    @Test
    public void testHintHandler() throws Exception
    {
        ParserContext parser = new ParserContext(furnace);
        File fXmlFile = new File(HINT_XML_FILE);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> hintList = $(doc).children("hint").get();

        Assert.assertEquals(3, hintList.size());

        Element firstHint = hintList.get(0);
        Hint hint = parser.processElement(firstHint);
        Assert.assertEquals("testVariable", hint.getVariableName());
        Assert.assertEquals(5, hint.getEffort());
        Assert.assertEquals(Severity.OPTIONAL, hint.getSeverity());
        Assert.assertEquals("test message", hint.getHintText().toString());
        Assert.assertEquals(1, hint.getLinks().size());
        List<Link> links = hint.getLinks();
        Assert.assertEquals("someUrl", links.get(0).getLink());
        Assert.assertEquals("someDescription", links.get(0).getTitle());


        Element secondHint = hintList.get(1);
        hint = parser.processElement(secondHint);
        Assert.assertEquals(null, hint.getVariableName());
        Assert.assertEquals(0, hint.getEffort());
        Assert.assertEquals(Severity.MANDATORY, hint.getSeverity());
        Assert.assertEquals("test-message", hint.getHintText().toString());
        Assert.assertEquals(3, hint.getLinks().size());
        links = hint.getLinks();
        Assert.assertEquals("url1", links.get(0).getLink());
        Assert.assertEquals("url2", links.get(1).getLink());
        Assert.assertEquals("url3", links.get(2).getLink());
        Assert.assertEquals("description1", links.get(0).getTitle());
        Assert.assertEquals("description2", links.get(1).getTitle());
        Assert.assertEquals("description3", links.get(2).getTitle());

        Set<String> tags = hint.getTags();
        Assert.assertTrue(tags.contains("java-ee-6"));
        Assert.assertTrue(tags.contains("jpa-2"));
        Assert.assertTrue(tags.contains("jpa"));
        Assert.assertFalse(tags.contains("foo"));


    }

    @Test(expected = WindupException.class)
    public void testXmlFileWithoutPublidIdAndXpath() throws Exception
    {
        ParserContext parser = new ParserContext(furnace);
        File fXmlFile = new File(HINT_XML_FILE);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> hintList = $(doc).children("hint").get();
        Element firstHint = hintList.get(2);
        parser.<Hint> processElement(firstHint);
    }
}