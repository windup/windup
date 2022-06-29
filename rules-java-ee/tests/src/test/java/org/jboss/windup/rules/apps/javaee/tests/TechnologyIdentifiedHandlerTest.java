package org.jboss.windup.rules.apps.javaee.tests;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.Furnace;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.rules.apps.javaee.TechnologyIdentified;
import org.jboss.windup.rules.apps.javaee.TechnologyIdentifiedHandler;
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
public class TechnologyIdentifiedHandlerTest {

    private static final String TECHNOLOGY_IDENTIFIED_XML_WINDUP_FILE = "src/test/resources/technology-identified/technology-identified.windup.xml";

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config-xml"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")})
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Inject
    private Furnace furnace;

    @Test
    public void testParsing() throws Exception {
        File fXmlFile = new File(TECHNOLOGY_IDENTIFIED_XML_WINDUP_FILE);
        testTechnologyIdentifiedHandler(fXmlFile);
    }

    public void testTechnologyIdentifiedHandler(File fXmlFile) throws Exception {
        RuleLoaderContext loaderContext = new RuleLoaderContext(Collections.singleton(fXmlFile.toPath()), null);
        ParserContext parser = new ParserContext(furnace, loaderContext);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        List<Element> technologyIdentifiedList = $(doc).children(TechnologyIdentifiedHandler.TAG_NAME).get();
        Assert.assertEquals(1, technologyIdentifiedList.size());
        Element firstTechnologyIdentified = technologyIdentifiedList.get(0);
        TechnologyIdentified technologyIdentified = parser.processElement(firstTechnologyIdentified);

        Assert.assertEquals("test-1", technologyIdentified.getTechnologyName());
        Assert.assertEquals(1, technologyIdentified.getCount());
        Assert.assertEquals(2, technologyIdentified.getTags().size());
        Assert.assertTrue(technologyIdentified.getTags().contains("tag-1"));
        Assert.assertTrue(technologyIdentified.getTags().contains("tag-2"));

    }
}