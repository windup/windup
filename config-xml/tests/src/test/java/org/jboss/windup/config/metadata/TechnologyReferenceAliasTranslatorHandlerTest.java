package org.jboss.windup.config.metadata;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.versions.Versions;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class TechnologyReferenceAliasTranslatorHandlerTest
{
    private static final String XML_FILE = "src/test/resources/testxml/testtransformers.windup.technologytransformer.xml";

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-xml"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Inject
    private Furnace furnace;

    @Inject
    private GraphContextFactory graphContextFactory;

    @Test
    public void testXmlParsinfOfRulesetMetadata() throws Exception
    {
        File fXmlFile = new File(XML_FILE);
        RuleLoaderContext loaderContext = new RuleLoaderContext(Collections.singleton(fXmlFile.toPath()), null);
        ParserContext parser = new ParserContext(furnace, loaderContext);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document firstXmlFile = dBuilder.parse(fXmlFile);
        List<TechnologyReferenceAliasTranslator> transformers = parser.processElement(firstXmlFile.getDocumentElement());

        // verify xmlfile
        Assert.assertEquals(2, transformers.size());
        Assert.assertEquals("sampleinput1", transformers.get(0).getOriginalTechnology().getId());
        Assert.assertEquals("[1]", transformers.get(0).getOriginalTechnology().getVersionRange().toString());
        Assert.assertEquals("sampleoutput2", transformers.get(0).getTargetTechnology().getId());
        Assert.assertEquals("[2]", transformers.get(0).getTargetTechnology().getVersionRange().toString());

        TechnologyReference input1 = new TechnologyReference("sampleinput1", Versions.parseVersionRange("[1]"));
        TechnologyReference output2 = transformers.get(0).translate(input1);
        Assert.assertEquals("sampleoutput2", output2.getId());
        Assert.assertEquals("[2]", output2.getVersionRange().toString());

        Assert.assertEquals("sampleinput3", transformers.get(1).getOriginalTechnology().getId());
        Assert.assertEquals("[3]", transformers.get(1).getOriginalTechnology().getVersionRange().toString());
        Assert.assertEquals("sampleoutput4", transformers.get(1).getTargetTechnology().getId());
        Assert.assertEquals("[4]", transformers.get(1).getTargetTechnology().getVersionRange().toString());
    }
}
