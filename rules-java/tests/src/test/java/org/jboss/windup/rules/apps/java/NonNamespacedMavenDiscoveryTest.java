package org.jboss.windup.rules.apps.java;

import com.google.common.collect.Iterables;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.engine.predicates.RuleProviderWithDependenciesPredicate;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.rulefilters.AndPredicate;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectDependencyModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class NonNamespacedMavenDiscoveryTest {
    @Inject
    private WindupProcessor processor;
    @Inject
    private GraphContextFactory factory;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Test
    public void testMetadataFound() throws Exception {
        String inputPath = "src/test/resources/NonNamespacedMavenDiscoveryTest";
        final Path outputPath = getDefaultPath();
        FileUtils.deleteDirectory(outputPath.toFile());
        Files.createDirectories(outputPath);
        try (GraphContext context = factory.create(outputPath, true)) {
            final WindupConfiguration processorConfig = new WindupConfiguration();
            processorConfig.setOptionValue(SourceModeOption.NAME, true);

            Predicate<RuleProvider> ruleFilter = new AndPredicate(new RuleProviderWithDependenciesPredicate(MigrationRulesPhase.class));

            processorConfig.setRuleProviderFilter(ruleFilter);
            processorConfig.setGraphContext(context);
            processorConfig.addInputPath(Paths.get(inputPath));
            processorConfig.setOutputDirectory(outputPath);
            processorConfig.setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""));

            processor.execute(processorConfig);

            ProjectModel project = WindupConfigurationService.getConfigurationModel(context).getInputPaths().iterator().next().getProjectModel();
            Assert.assertTrue("Maven Project Expected", project instanceof MavenProjectModel);

            MavenProjectModel mavenProject = (MavenProjectModel) project;
            Assert.assertEquals("testgroupid", mavenProject.getGroupId());
            Assert.assertEquals("testartifactid", mavenProject.getArtifactId());
            Assert.assertEquals("testname", mavenProject.getName());
            Assert.assertEquals("1.0.0.testversion", mavenProject.getVersion());
            Assert.assertEquals("testdescription", mavenProject.getDescription());

            Assert.assertEquals(1, Iterables.size(mavenProject.getDependencies()));
            ProjectDependencyModel dependency = mavenProject.getDependencies().iterator().next();

            Assert.assertEquals("testdependency-groupid", ((MavenProjectModel) dependency.getProjectModel()).getGroupId());
            Assert.assertEquals("testdependency-artifactid", ((MavenProjectModel) dependency.getProjectModel()).getArtifactId());
            Assert.assertEquals("1.4", dependency.getProjectModel().getVersion());
        }
    }

    private Path getDefaultPath() {
        return FileUtils.getTempDirectory().toPath().resolve("Windup")
                .resolve("windupgraph_nonnamespacedmavendisc_" + RandomStringUtils.randomAlphanumeric(6));
    }

}
