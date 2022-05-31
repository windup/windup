package org.jboss.windup.reporting.handlers;

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
import org.jboss.windup.reporting.config.HasHint;
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

/**
 * Created by mbriskar on 6/9/15.
 */
@RunWith(Arquillian.class)
public class HasHintHandlerTest {

    private static final String HINT_XML_WINDUP_FILE = "src/test/resources/handler/hashint.windup.xml";
    private static final String HINT_XML_RHAMT_FILE = "src/test/resources/handler/hashint.rhamt.xml";
    private static final String HINT_XML_MTA_FILE = "src/test/resources/handler/hashint.mta.xml";
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
        return ShrinkWrap
                .create(AddonArchive.class)
                .addBeansXML();
    }

    @Test
    public void testWindupHintHandler() throws Exception {
        File fXmlFile = new File(HINT_XML_WINDUP_FILE);
        testHintHandler(fXmlFile);
    }

    @Test
    public void testRhamtHintHandler() throws Exception {
        File fXmlFile = new File(HINT_XML_RHAMT_FILE);
        testHintHandler(fXmlFile);
    }

    @Test
    public void testMtaHintHandler() throws Exception {
        File fXmlFile = new File(HINT_XML_MTA_FILE);
        testHintHandler(fXmlFile);
    }

    public void testHintHandler(File fXmlFile) throws Exception {
        RuleLoaderContext loaderContext = new RuleLoaderContext(Collections.singleton(fXmlFile.toPath()), null);
        ParserContext parser = new ParserContext(furnace, loaderContext);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> hintList = $(doc).children("has-hint").get();
        Element firstHint = hintList.get(0);
        HasHint hint = parser.processElement(firstHint);

        Assert.assertNull(hint.getInputVariablesName());
        Assert.assertEquals(Iteration.DEFAULT_VARIABLE_LIST_STRING, hint.getOutputVariablesName());
        Assert.assertNull(hint.getMessagePattern());

        Element secondHint = hintList.get(1);
        hint = parser.processElement(secondHint);
        Assert.assertNull(hint.getInputVariablesName());
        Assert.assertEquals(Iteration.DEFAULT_VARIABLE_LIST_STRING, hint.getOutputVariablesName());
        Assert.assertTrue(hint.getMessagePattern().contains("test-message"));

        Element thirdHint = hintList.get(2);
        hint = parser.processElement(thirdHint);
        Assert.assertNull(hint.getInputVariablesName());
        Assert.assertEquals(Iteration.DEFAULT_VARIABLE_LIST_STRING, hint.getOutputVariablesName());
        Assert.assertTrue(hint.getMessagePattern().contains("test-message"));

    }
}