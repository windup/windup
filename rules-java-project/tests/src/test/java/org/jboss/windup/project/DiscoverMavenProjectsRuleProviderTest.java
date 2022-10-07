package org.jboss.windup.project;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.engine.predicates.RuleProviderWithDependenciesPredicate;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.DependencyLocation;
import org.jboss.windup.project.condition.Artifact;
import org.jboss.windup.project.condition.Project;
import org.jboss.windup.rules.apps.java.condition.Version;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RunWith(Arquillian.class)
public class DiscoverMavenProjectsRuleProviderTest {
    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-project")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Inject
    ProjectWithPluginDependencyTestRuleProvider provider;

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testPluginDependencies() throws IOException, InstantiationException, IllegalAccessException {
        final Path inputDir = Paths.get("src/test/resources/pom");
        final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "windup_" + RandomStringUtils.randomAlphanumeric(6));

        try (GraphContext context = factory.create(getDefaultPath(), true)) {
            final WindupConfiguration processorConfig = new WindupConfiguration();
            processorConfig.setRuleProviderFilter(new RuleProviderWithDependenciesPredicate(ProjectWithPluginDependencyTestRuleProvider.class));
            processorConfig.setGraphContext(context);
            processorConfig.addInputPath(inputDir);
            processorConfig.setOutputDirectory(outputPath);
            processorConfig.setOptionValue(SourceModeOption.NAME, true);

            processor.execute(processorConfig);

            Assert.assertEquals(1, provider.getPluginDependencyCount());
        }
    }

    private Path getDefaultPath() {
        return FileUtils.getTempDirectory().toPath().resolve("Windup")
                .resolve("windupgraph_discovermavenprojectsruleprovidertest" + RandomStringUtils.randomAlphanumeric(6));
    }

    private static int dependencyCount = 0;

    @After
    public void after() {
        dependencyCount = 0;
    }

    @Singleton
    public static class ProjectWithPluginDependencyTestRuleProvider extends AbstractRuleProvider {
        public ProjectWithPluginDependencyTestRuleProvider() {
            super(MetadataBuilder.forProvider(ProjectWithPluginDependencyTestRuleProvider.class));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            List<DependencyLocation> locations = List.of(DependencyLocation.DEPENDENCY_MANAGEMENT);
            Artifact artifact = Artifact.withGroupId("it.unimi.dsi").andArtifactId("fastutil").andVersion(Version.fromVersion("6.5.5").to("6.5.8")).andLocations(locations);
            return ConfigurationBuilder.begin()
                    .addRule().when(
                            Project.dependsOnArtifact(artifact)
                    ).perform(new GraphOperation() {
                                  @Override
                                  public void perform(GraphRewrite event, EvaluationContext context) {
                                      dependencyCount++;
                                  }
                              }
                    );
        }

        public int getPluginDependencyCount() {
            return dependencyCount;
        }
    }
}
