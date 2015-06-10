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
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.reporting.config.condition.HintExists;
import org.jboss.windup.reporting.model.Severity;
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
import java.util.List;

import static org.joox.JOOX.$;

/**
 * Created by mbriskar on 6/9/15.
 */
@RunWith(Arquillian.class)
public class HintExistsHandlerTest
{

    private static final String HINT_XML_FILE = "src/test/resources/handler/hintexists.windup.xml";

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
        List<Element> hintList = $(doc).children("hint-exists").get();
        Element firstHint = hintList.get(0);
        HintExists hint = parser.processElement(firstHint);

        Assert.assertEquals(null, hint.getInputVariablesName());
        Assert.assertEquals(Iteration.DEFAULT_VARIABLE_LIST_STRING, hint.getOutputVariablesName());
        Assert.assertEquals(null, hint.getFilename());
        Assert.assertEquals(null, hint.getMessagePattern());

        Element secondHint = hintList.get(1);
        hint = parser.processElement(secondHint);
        Assert.assertEquals(null, hint.getInputVariablesName());
        Assert.assertEquals(Iteration.DEFAULT_VARIABLE_LIST_STRING, hint.getOutputVariablesName());
        Assert.assertTrue(hint.getMessagePattern().contains("test-message"));
        Assert.assertEquals(null, hint.getFilename());

        Element thirdHint = hintList.get(2);
        hint = parser.processElement(thirdHint);
        Assert.assertEquals(null, hint.getInputVariablesName());
        Assert.assertEquals(Iteration.DEFAULT_VARIABLE_LIST_STRING, hint.getOutputVariablesName());
        Assert.assertTrue(hint.getMessagePattern().contains("test-message"));
        Assert.assertEquals("test-in", hint.getFilename());

    }
}
