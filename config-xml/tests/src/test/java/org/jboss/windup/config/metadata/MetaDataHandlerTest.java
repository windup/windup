package org.jboss.windup.config.metadata;

import java.io.File;
import java.util.Collections;
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
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.phase.PostMigrationRulesPhase;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.w3c.dom.Document;

@RunWith(Arquillian.class)
public class MetaDataHandlerTest
{

    private static final String XML_WINDUP_FILE = "src/test/resources/testxml/metadata.windup.xml";
    private static final String XML_WINDUP_WITH_OVERRIDE_FILE = "src/test/resources/testxml/metadata.override.windup.xml";
    private static final String XML_RHAMT_FILE = "src/test/resources/testxml/metadata.rhamt.xml";
    private static final String XML_RHAMT_WITH_OVERRIDE_FILE = "src/test/resources/testxml/metadata.override.rhamt.xml";
    private static final String XML_MTA_FILE = "src/test/resources/testxml/metadata.mta.xml";
    private static final String XML_MTA_WITH_OVERRIDE_FILE = "src/test/resources/testxml/metadata.override.mta.xml";

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
    public void testWindupXmlParsinfOfRulesetMetadata() throws Exception
    {
        File fXmlFile = new File(XML_WINDUP_FILE);
        testXmlParsinfOfRulesetMetadata(fXmlFile);
    }

    @Test
    public void testRhamtXmlParsinfOfRulesetMetadata() throws Exception
    {
        File fXmlFile = new File(XML_RHAMT_FILE);
        testXmlParsinfOfRulesetMetadata(fXmlFile);
    }

    @Test
    public void testMtaXmlParsinfOfRulesetMetadata() throws Exception
    {
        File fXmlFile = new File(XML_MTA_FILE);
        testXmlParsinfOfRulesetMetadata(fXmlFile);
    }

    private void testXmlParsinfOfRulesetMetadata(File fXmlFile) throws Exception
    {
        RuleLoaderContext loaderContext = new RuleLoaderContext(Collections.singleton(fXmlFile.toPath()), null);
        ParserContext parser = new ParserContext(furnace, loaderContext);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document firstXmlFile = dBuilder.parse(fXmlFile);
        parser.processElement(firstXmlFile.getDocumentElement());
        // verify xmlfile
        Assert.assertEquals(1, parser.getRuleProviders().size());
        AbstractRuleProvider abstractRuleProvider = parser.getRuleProviders().get(0);
        RuleProviderMetadata metadata = abstractRuleProvider.getMetadata();
        Class<? extends RulePhase> phase = metadata.getPhase();
        List<String> executeAfterIDs = metadata.getExecuteAfterIDs();
        List<String> executeBeforeIDs = metadata.getExecuteBeforeIDs();
        Set<AddonId> requiredAddons = metadata.getRequiredAddons();
        Set<TechnologyReference> sourceTechnologies = metadata.getSourceTechnologies();
        Set<TechnologyReference> targetTechnologies = metadata.getTargetTechnologies();
        Set<String> tags = metadata.getTags();

        Assert.assertTrue(PostMigrationRulesPhase.class.isAssignableFrom(phase));
        Assert.assertTrue(executeAfterIDs.contains("AfterId"));
        Assert.assertTrue(executeBeforeIDs.contains("BeforeId"));
        Assert.assertTrue(tags.contains("require-stateless"));
        Assert.assertTrue(tags.contains("require-nofilesystem-io"));
        Assert.assertTrue(requiredAddons.contains(AddonId.valueOf("org.jboss.windup.rules,windup-rules-javaee,2.0.1.Final")));
        Assert.assertTrue(requiredAddons.contains(AddonId.valueOf("org.jboss.windup.rules,windup-rules-java,2.0.0.Final")));
        Assert.assertTrue(sourceTechnologies.contains(new TechnologyReference("ejb", "(2,3]")));
        Assert.assertTrue(sourceTechnologies.contains(new TechnologyReference("weblogic", "(10,12]")));
        Assert.assertTrue(sourceTechnologies.contains(new TechnologyReference("servlet")));
        Assert.assertTrue(targetTechnologies.contains(new TechnologyReference("eap", "(5,6]")));
        Assert.assertTrue(targetTechnologies.contains(new TechnologyReference("ejb", "(2,3]")));
        Assert.assertTrue(targetTechnologies.contains(new TechnologyReference("ejb", "(2,3]")));
        Assert.assertTrue(targetTechnologies.contains(new TechnologyReference("jsp")));
        Assert.assertFalse(metadata.isOverrideProvider());

        Configuration configuration = abstractRuleProvider.getConfiguration(null);
        Assert.assertFalse(configuration.getRules().isEmpty());
        Assert.assertTrue(configuration.getRules().get(0).toString().contains("test {foo} iteration perform"));
    }

    @Test
    public void testWindupXmlRuleOverrideProviderMetadata() throws Exception
    {
        File fXmlFile = new File(XML_WINDUP_WITH_OVERRIDE_FILE);
        testXmlRuleOverrideProviderMetadata(fXmlFile);
    }

    @Test
    public void testRhamtXmlRuleOverrideProviderMetadata() throws Exception
    {
        File fXmlFile = new File(XML_RHAMT_WITH_OVERRIDE_FILE);
        testXmlRuleOverrideProviderMetadata(fXmlFile);
    }

    @Test
    public void testMtaXmlRuleOverrideProviderMetadata() throws Exception
    {
        File fXmlFile = new File(XML_MTA_WITH_OVERRIDE_FILE);
        testXmlRuleOverrideProviderMetadata(fXmlFile);
    }

    public void testXmlRuleOverrideProviderMetadata(File fXmlFile) throws Exception
    {
        RuleLoaderContext loaderContext = new RuleLoaderContext(Collections.singleton(fXmlFile.toPath()), null);
        ParserContext parser = new ParserContext(furnace, loaderContext);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document firstXmlFile = dBuilder.parse(fXmlFile);
        parser.processElement(firstXmlFile.getDocumentElement());

        // verify xmlfile
        Assert.assertEquals(1, parser.getRuleProviders().size());
        AbstractRuleProvider abstractRuleProvider = parser.getRuleProviders().get(0);
        RuleProviderMetadata metadata = abstractRuleProvider.getMetadata();

        Assert.assertTrue(metadata.isOverrideProvider());
    }
}
