package org.jboss.windup.config.metadata;

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
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
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

    private static final String XML_FILE = "src/test/resources/testxml/metadata.windup.xml";

    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config-xml"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment()
    {
        final AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                    .addBeansXML()
                    .addClass(MetaDataHandlerTest.class)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config-xml"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );

        return archive;
    }

    @Inject
    private Furnace furnace;

    @Inject
    private GraphContextFactory graphContextFactory;

    @Test
    public void testXmlParsinfOfRulesetMetadata() throws Exception
    {
        ParserContext parser = new ParserContext(furnace);
        File fXmlFile = new File(XML_FILE);
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

        try (GraphContext graphContext = graphContextFactory.create())
        {
            Configuration configuration = abstractRuleProvider.getConfiguration(graphContext);
            Assert.assertFalse(configuration.getRules().isEmpty());
            Assert.assertTrue(configuration.getRules().get(0).toString().contains("test {foo} iteration perform"));
        }
    }

}
