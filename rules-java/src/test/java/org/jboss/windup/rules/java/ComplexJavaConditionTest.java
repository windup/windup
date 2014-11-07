package org.jboss.windup.rules.java;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.gremlinquery.GremlinQuery;
import org.jboss.windup.config.gremlinquery.DebugStep;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.model.FileLocationModel;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

@RunWith(Arquillian.class)
public class ComplexJavaConditionTest
{

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-xml"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-xml"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );

        return archive;
    }

    @Inject
    private TestComplexJavaConditionGremlinRuleProvider providerGremlinQuery;

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testGremlinQuery() throws IOException
    {
        Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "Windup",
                    "complexjavaconditiontest_" + RandomStringUtils.randomAlphanumeric(6));
        try (GraphContext context = factory.create(outputPath.resolve("graph")))
        {
            Predicate<WindupRuleProvider> predicate = new Predicate<WindupRuleProvider>()
            {
                @Override
                public boolean accept(WindupRuleProvider provider)
                {
                    return (provider.getPhase() != RulePhase.MIGRATION_RULES);
                }
            };
            WindupConfiguration windupConfiguration = new WindupConfiguration()
                        .setRuleProviderFilter(predicate)
                        .setGraphContext(context);
            windupConfiguration.setOptionValue(ScanPackagesOption.NAME, Collections.singletonList("com."));
            windupConfiguration.setInputPath(Paths.get("../test-files/jee-example-app-1.0.0.ear"));
            windupConfiguration.setOutputDirectory(outputPath);
            processor.execute(windupConfiguration);
        }
    }

    @Singleton
    public static class TestComplexJavaConditionGremlinRuleProvider extends WindupRuleProvider
    {
        private Set<FileLocationModel> xmlFiles = new HashSet<>();

        @Override
        public RulePhase getPhase()
        {
            return RulePhase.POST_MIGRATION_RULES;
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            AbstractIterationOperation<FileLocationModel> addTypeRefToList = new AbstractIterationOperation<FileLocationModel>()
            {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context, FileLocationModel payload)
                {
                    xmlFiles.add(payload);
                }
            };

            return ConfigurationBuilder
                        .begin()
                        .addRule()
                        .when(
                                GremlinQuery.fromType(XmlFileModel.class)
                                    .step(
                                        XmlFile.matchesXpath("/ejb:ejb-jar//ejb:remote/text()")
                                               .namespace("ejb", "http://java.sun.com/xml/ns/j2ee")
                                               
                                    )
                                    .step(DebugStep.output(2))
                                    .step(
                                        JavaClass.references(".*").inType("#{it.referenceSourceSnippit}").at(TypeReferenceLocation.TYPE)
                                    )
                        )
                        .perform(Hint.withText("Convert to EJB3 with Annotations Please!")
                                               .and(addTypeRefToList));
        }
        // @formatter:on

        public Set<FileLocationModel> getXmlFileMatches()
        {
            return xmlFiles;
        }
    }
}
