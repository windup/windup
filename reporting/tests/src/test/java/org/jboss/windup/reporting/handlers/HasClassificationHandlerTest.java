package org.jboss.windup.reporting.handlers;

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
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.reporting.config.HasClassification;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by mbriskar on 6/9/15.
 */
@RunWith(Arquillian.class)
public class HasClassificationHandlerTest
{

    private static final String HINT_XML_WINDUP_FILE = "src/test/resources/handler/hasclassification.windup.xml";
    private static final String HINT_XML_RHAMT_FILE = "src/test/resources/handler/hasclassification.rhamt.xml";
    private static final String HINT_XML_MTA_FILE = "src/test/resources/handler/hasclassification.mta.xml";

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
    public void testWindupHintHandler() throws Exception
    {
        File fXmlFile = new File(HINT_XML_WINDUP_FILE);
        testHintHandler(fXmlFile);
    }

    @Test
    public void testRhamtHintHandler() throws Exception
    {
        File fXmlFile = new File(HINT_XML_RHAMT_FILE);
        testHintHandler(fXmlFile);
    }

    @Test
    public void testMtaHintHandler() throws Exception
    {
        File fXmlFile = new File(HINT_XML_MTA_FILE);
        testHintHandler(fXmlFile);
    }

    public void testHintHandler(File fXmlFile) throws Exception
    {
        RuleLoaderContext loaderContext = new RuleLoaderContext(Collections.singleton(fXmlFile.toPath()), null);
        ParserContext parser = new ParserContext(furnace, loaderContext);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> classificationList = $(doc).children("has-classification").get();
        Element firstHint = classificationList.get(0);
        HasClassification classification = parser.processElement(firstHint);

        Assert.assertNull(classification.getInputVariablesName());
        Assert.assertEquals(Iteration.DEFAULT_VARIABLE_LIST_STRING, classification.getOutputVariablesName());
        Assert.assertNull(classification.getTitlePattern());

        Element secondHint = classificationList.get(1);
        classification = parser.processElement(secondHint);
        Assert.assertNull(classification.getInputVariablesName());
        Assert.assertEquals(Iteration.DEFAULT_VARIABLE_LIST_STRING, classification.getOutputVariablesName());
        Assert.assertTrue(classification.getTitlePattern().contains("test-message"));

        Element thirdHint = classificationList.get(2);
        classification = parser.processElement(thirdHint);
        Assert.assertNull(classification.getInputVariablesName());
        Assert.assertEquals(Iteration.DEFAULT_VARIABLE_LIST_STRING, classification.getOutputVariablesName());
        Assert.assertTrue(classification.getTitlePattern().contains("test-message"));

    }
}