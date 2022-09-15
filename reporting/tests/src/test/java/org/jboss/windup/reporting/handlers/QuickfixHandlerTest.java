package org.jboss.windup.reporting.handlers;

import static org.joox.JOOX.$;
import static org.junit.Assert.assertNotNull;

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
import org.jboss.windup.reporting.model.QuickfixType;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.reporting.quickfix.Quickfix;
import org.jboss.windup.reporting.config.classification.Classification;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RunWith(Arquillian.class)
public class QuickfixHandlerTest {
    private static final String QUICKFIX_XML_WINDUP_FILE = "src/test/resources/handler/quickfix.windup.xml";
    private static final String QUICKFIX_XML_RHAMT_FILE = "src/test/resources/handler/quickfix.rhamt.xml";
    private static final String QUICKFIX_XML_MTA_FILE = "src/test/resources/handler/quickfix.mta.xml";

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

    @Inject
    private Furnace furnace;

    @Test
    public void testWindupClassificationParsing() throws Exception {
        File fXmlFile = new File(QUICKFIX_XML_WINDUP_FILE);
        testClassificationParsing(fXmlFile);
    }

    @Test
    public void testRhamtClassificationParsing() throws Exception {
        File fXmlFile = new File(QUICKFIX_XML_RHAMT_FILE);
        testClassificationParsing(fXmlFile);
    }

    @Test
    public void testMtaClassificationParsing() throws Exception {
        File fXmlFile = new File(QUICKFIX_XML_MTA_FILE);
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
        Assert.assertEquals(2, classificationList.size());

        Element firstClassification = classificationList.get(0);
        Classification classification = parser.<Classification>processElement(firstClassification);

        Assert.assertEquals(0, classification.getEffort());
        Assert.assertEquals("test-classification", classification.getClassificationPattern().toString());
        Assert.assertEquals(0, classification.getLinks().size());
        Assert.assertEquals(2, classification.getQuickfixes().size());
        List<Quickfix> quickfixes = classification.getQuickfixes();
        checkQuickfix(quickfixes.get(0), "test1 qf", QuickfixType.INSERT_LINE, "something new", null, null);
        checkQuickfix(quickfixes.get(1), "test2 qf", QuickfixType.REPLACE, null, "test1", "test2");

        Element secondClassification = classificationList.get(1);
        classification = parser.<Classification>processElement(secondClassification);

        Assert.assertEquals(null, classification.getVariableName());
        Assert.assertEquals(1, classification.getEffort());
        Assert.assertEquals("test-message", classification.getClassificationPattern().toString());
        Assert.assertEquals(null, classification.getDescriptionPattern());
        Assert.assertEquals(3, classification.getLinks().size());
        List<Link> links = classification.getLinks();
        Assert.assertEquals("url1", links.get(0).getLink());
        Assert.assertEquals("url2", links.get(1).getLink());
        Assert.assertEquals("url3", links.get(2).getLink());
        Assert.assertEquals("description1", links.get(0).getTitle());
        Assert.assertEquals("description2", links.get(1).getTitle());
        Assert.assertEquals("description3", links.get(2).getTitle());

        Assert.assertEquals(1, classification.getQuickfixes().size());
        Quickfix quickfix = classification.getQuickfixes().get(0);
        assertNotNull(quickfix);
        checkQuickfix(quickfix, "test delete", QuickfixType.DELETE_LINE, null, null, null);
    }

    @Test
    public void testWindupHintParsing() throws Exception {
        File fXmlFile = new File(QUICKFIX_XML_WINDUP_FILE);
        testHintParsing(fXmlFile);
    }

    @Test
    public void testRhamtHintParsing() throws Exception {
        File fXmlFile = new File(QUICKFIX_XML_RHAMT_FILE);
        testHintParsing(fXmlFile);
    }

    @Test
    public void testMtaHintParsing() throws Exception {
        File fXmlFile = new File(QUICKFIX_XML_MTA_FILE);
        testHintParsing(fXmlFile);
    }

    public void testHintParsing(File fXmlFile) throws Exception {
        RuleLoaderContext loaderContext = new RuleLoaderContext(Collections.singleton(fXmlFile.toPath()), null);
        ParserContext parser = new ParserContext(furnace, loaderContext);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> hintList = $(doc).children("hint").get();

        Assert.assertEquals(1, hintList.size());

        Element hintElement = hintList.get(0);
        Hint hint = parser.processElement(hintElement);
        Assert.assertEquals("testVariable", hint.getVariableName());
        Assert.assertEquals(2, hint.getEffort());
        Assert.assertEquals("test message", hint.getHintText().toString());
        Assert.assertEquals(2, hint.getLinks().size());
        List<Link> links = hint.getLinks();
        Assert.assertEquals("url1", links.get(0).getLink());
        Assert.assertEquals("description1", links.get(0).getTitle());

        Assert.assertEquals(3, hint.getQuickfixes().size());
        checkQuickfix(hint.getQuickfixes().get(0), "test1 quickfix", QuickfixType.DELETE_LINE, null, null, null);
        checkQuickfix(hint.getQuickfixes().get(2), "test3 quickfix", QuickfixType.REPLACE, null, "what", "when");
        checkQuickfix(hint.getQuickfixes().get(1), "test2 quickfix", QuickfixType.INSERT_LINE, "something new", null, null);
    }

    private void checkQuickfix(Quickfix fix, String name, QuickfixType type, String newlineStr, String searchStr, String replacementStr) {
        System.out.println(fix);

        Assert.assertEquals(name, fix.getName());
        Assert.assertEquals(type, fix.getType());
        if (type == QuickfixType.INSERT_LINE) {
            Assert.assertEquals(newlineStr, fix.getNewline());
        } else if (type == QuickfixType.REPLACE) {
            Assert.assertEquals(replacementStr, fix.getReplacementStr());
            Assert.assertEquals(searchStr, fix.getSearchStr());
        }

    }

}