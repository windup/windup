package org.jboss.windup.reporting.handlers;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.Furnace;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.reporting.config.condition.ClassificationExists;
import org.jboss.windup.reporting.config.condition.HintExists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.List;

import static org.joox.JOOX.$;

/**
 * Created by mbriskar on 6/9/15.
 */
@RunWith(Arquillian.class)
public class ClassificationExistsHandlerTest
{

    private static final String HINT_XML_FILE = "src/test/resources/handler/classificationexists.windup.xml";

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
        List<Element> classificationList = $(doc).children("classification-exists").get();
        Element firstHint = classificationList.get(0);
        ClassificationExists classification = parser.processElement(firstHint);

        Assert.assertEquals(null, classification.getInputVariablesName());
        Assert.assertEquals(Iteration.DEFAULT_VARIABLE_LIST_STRING, classification.getOutputVariablesName());
        Assert.assertEquals(null, classification.getFilename());
        Assert.assertEquals(null, classification.getClassificationPattern());

        Element secondHint = classificationList.get(1);
        classification = parser.processElement(secondHint);
        Assert.assertEquals(null, classification.getInputVariablesName());
        Assert.assertEquals(Iteration.DEFAULT_VARIABLE_LIST_STRING, classification.getOutputVariablesName());
        Assert.assertTrue(classification.getClassificationPattern().contains("test-message"));
        Assert.assertEquals(null, classification.getFilename());

        Element thirdHint = classificationList.get(2);
        classification = parser.processElement(thirdHint);
        Assert.assertEquals(null, classification.getInputVariablesName());
        Assert.assertEquals(Iteration.DEFAULT_VARIABLE_LIST_STRING, classification.getOutputVariablesName());
        Assert.assertTrue(classification.getClassificationPattern().contains("test-message"));
        Assert.assertEquals("test-in", classification.getFilename());

    }
}