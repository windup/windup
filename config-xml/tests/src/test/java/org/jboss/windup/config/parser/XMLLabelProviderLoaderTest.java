package org.jboss.windup.config.parser;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.LabelProvider;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.loader.LabelProviderLoader;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.Label;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.And;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.DefaultOperationBuilder;
import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.config.RuleBuilder;
import org.ocpsoft.rewrite.config.True;

@RunWith(Arquillian.class)
public class XMLLabelProviderLoaderTest
{
    private static final Logger LOG = Logger.getLogger(XMLLabelProviderLoaderTest.class.getName());

    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config-xml"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap.create(AddonArchive.class)
                    .addBeansXML()
                    .addAsResource(new File("src/test/resources/labeltestxml/Test1.windup.label.xml"));
    }

    @Deployment(name = "rhamt,1")
    public static AddonArchive getRhamtDeployment()
    {
        return ShrinkWrap.create(AddonArchive.class)
                    .addBeansXML()
                    .addAsResource(new File("src/test/resources/labeltestxml/Test2.rhamt.label.xml"));
    }

    @Inject
    private LabelProviderLoader loader;

    @Test
    public void testGetProviders() throws Exception
    {
        Assert.assertNotNull(loader);

        RuleLoaderContext ruleLoaderContext = new RuleLoaderContext();
        List<LabelProvider> providers = loader.getProviders(ruleLoaderContext);
        Assert.assertNotNull(providers);
        Assert.assertEquals(2, providers.size());


        LabelProvider provider = providers
                .stream()
                .filter(p -> p.getMetadata().getID().equals("testlabelprovider1"))
                .findFirst()
                .orElseThrow(() -> new IllegalAccessError("No testlabelprovider1 found"));
        checkWindupMetadata(provider);
        List<Label> labels = provider.getData().getLabels();
        Assert.assertEquals(2, labels.size());

        Label label = labels.get(0);
        checkLabel1(label);

        label = labels.get(1);
        checkLabel2(label);


        provider = providers
                .stream()
                .filter(p -> p.getMetadata().getID().equals("testlabelprovider2"))
                .findFirst()
                .orElseThrow(() -> new IllegalAccessError("No testlabelprovider2 found"));
        checkRhamtMetadata(provider);
        labels = provider.getData().getLabels();
        Assert.assertEquals(3, labels.size());

        label = labels.get(0);
        checkLabel1(label);

        label = labels.get(1);
        checkLabel2(label);
    }

    private void checkWindupMetadata(LabelProvider provider)
    {
        String id = provider.getMetadata().getID();
        Assert.assertEquals("testlabelprovider1", id);
        Assert.assertNull(provider.getMetadata().getDescription());
        Assert.assertEquals(1, provider.getMetadata().getPriority());
        Assert.assertTrue(provider.getMetadata().getOrigin().matches("jar:file:.*/DEFAULT.*/Test1.windup.label.xml"));
    }

    private void checkRhamtMetadata(LabelProvider provider)
    {
        String id = provider.getMetadata().getID();
        Assert.assertEquals("testlabelprovider2", id);
        Assert.assertNull(provider.getMetadata().getDescription());
        Assert.assertTrue(provider.getMetadata().getOrigin().matches("jar:file:.*/rhamt-1.*/Test2.rhamt.label.xml"));
    }

    private void checkLabel1(Label label)
    {
        Set<String> supported = label.getSupported();
        Assert.assertEquals(1, supported.size());

        Set<String> unsuitable = label.getUnsuitable();
        Assert.assertEquals(2, unsuitable.size());

        Set<String> neutral = label.getNeutral();
        Assert.assertEquals(3, neutral.size());
    }

    private void checkLabel2(Label label)
    {
        Set<String> supported = label.getSupported();
        Assert.assertEquals(1, supported.size());

        Set<String> unsuitable = label.getUnsuitable();
        Assert.assertEquals(1, unsuitable.size());

        Set<String> neutral = label.getNeutral();
        Assert.assertEquals(0, neutral.size());
    }
}
