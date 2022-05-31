package org.jboss.windup.rules.xml;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.PostMigrationRulesPhase;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.xml.DiscoverXmlFilesRuleProvider;
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.testutil.basics.WindupTestUtilMethods;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@RunWith(Arquillian.class)
public class XmlFileDtdNamespaceTest {

    @Inject
    private WindupProcessor processor;
    @Inject
    private GraphContextFactory factory;
    @Inject
    private TestXMLNestedXmlFileRuleProvider provider;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-base"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-xml"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML().addClass(TestXMLNestedXmlFileRuleProvider.class).addClass(WindupTestUtilMethods.class);
    }

    @Test
    public void testRuleProviders() throws Exception {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder, true)) {
            initData(context);
            List<RuleProvider> providers = new ArrayList<>();
            providers.add(new DiscoverXmlFilesRuleProvider());
            providers.add(provider);
            WindupTestUtilMethods.runOnlyRuleProviders(providers, context);
            Assert.assertEquals(1, provider.counterStatic);
            Assert.assertEquals(1, provider.counterInner);
        }
    }

    public void initData(GraphContext context) {
        XmlFileModel xmlFileWithDtdNamespace = context.getFramed().addFramedVertex(XmlFileModel.class);
        xmlFileWithDtdNamespace.setFilePath("src/test/resources/dtd-namespace-test.xml");
    }

    @Singleton
    public static class TestXMLNestedXmlFileRuleProvider extends AbstractRuleProvider {
        public int counterStatic = 0;
        public int counterInner = 0;

        public TestXMLNestedXmlFileRuleProvider() {
            super(MetadataBuilder.forProvider(TestXMLNestedXmlFileRuleProvider.class).setPhase(PostMigrationRulesPhase.class));
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(XmlFile.withDTDSystemId("http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd"))
                    .perform(new AbstractIterationOperation<WindupVertexFrame>() {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, WindupVertexFrame payload) {
                            counterStatic++;
                        }
                    })
                    .addRule()
                    .when(XmlFile.matchesXpath("//hibernate-mapping").andDTDSystemId("http://www.hibernate.org/dtd/hibernate-mapping-3.0.dtd"))
                    .perform(new AbstractIterationOperation<WindupVertexFrame>() {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, WindupVertexFrame payload) {
                            counterInner++;
                        }
                    });
        }
    }

}