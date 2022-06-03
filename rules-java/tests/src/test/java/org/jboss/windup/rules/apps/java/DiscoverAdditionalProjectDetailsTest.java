package org.jboss.windup.rules.apps.java;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import javax.inject.Inject;

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
import org.jboss.windup.engine.predicates.EnumeratedRuleProviderPredicate;
import org.jboss.windup.engine.predicates.RuleProviderWithDependenciesPredicate;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.rulefilters.AndPredicate;
import org.jboss.windup.exec.rulefilters.NotPredicate;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.DuplicateArchiveModel;
import org.jboss.windup.graph.service.ArchiveService;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.java.decompiler.BeforeDecompileClassesRuleProvider;
import org.jboss.windup.rules.apps.java.decompiler.DecompileClassesRuleProvider;
import org.jboss.windup.rules.apps.java.model.JarManifestModel;
import org.jboss.windup.rules.apps.java.service.JarManifestService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class DiscoverAdditionalProjectDetailsTest
{
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
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Test
    public void testLog4jNameFound() throws Exception
    {
        String inputPath = "../../test-files/jee-example-app-1.0.0.ear";
        final Path outputPath = getDefaultPath();
        FileUtils.deleteDirectory(outputPath.toFile());
        Files.createDirectories(outputPath);
        try (GraphContext context = factory.create(outputPath, true))
        {
            final WindupConfiguration processorConfig = new WindupConfiguration();
            processorConfig.setOptionValue(SourceModeOption.NAME, false);

            Predicate<RuleProvider> ruleFilter = new AndPredicate(new RuleProviderWithDependenciesPredicate(MigrationRulesPhase.class),
                        new NotPredicate(new EnumeratedRuleProviderPredicate(DecompileClassesRuleProvider.class,
                                    BeforeDecompileClassesRuleProvider.class)));

            processorConfig.setRuleProviderFilter(ruleFilter);
            processorConfig.setGraphContext(context);
            processorConfig.addInputPath(Paths.get(inputPath));
            processorConfig.setOutputDirectory(outputPath);
            processorConfig.setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""));

            processor.execute(processorConfig);

            ArchiveService archiveService = new ArchiveService(context);
            boolean manifestFound = false;
            boolean jarNameFound = false;
            boolean jarVersionFound = false;
            for (ArchiveModel archiveModel : archiveService.findAllByProperty(ArchiveModel.ARCHIVE_NAME, "log4j-1.2.6.jar"))
            {
                if (archiveModel instanceof DuplicateArchiveModel)
                    archiveModel = ((DuplicateArchiveModel)archiveModel).getCanonicalArchive();

                JarManifestService jarManifestService = new JarManifestService(context);
                for (JarManifestModel manifest : jarManifestService.getManifestsByArchive(archiveModel))
                {
                    manifestFound = true;

                    if ("log4j".equals(manifest.getName()))
                        jarNameFound = true;

                    if ("1.2.6".equals(manifest.getVersion()))
                        jarVersionFound = true;
                }

                Assert.assertNotNull(archiveModel.getProjectModel());
                Assert.assertEquals("log4j", archiveModel.getProjectModel().getName());
                Assert.assertEquals("1.2.6", archiveModel.getProjectModel().getVersion());
            }
            Assert.assertTrue(manifestFound);
            Assert.assertTrue(jarNameFound);
            Assert.assertTrue(jarVersionFound);
        }
    }

    private Path getDefaultPath()
    {
        return FileUtils.getTempDirectory().toPath().resolve("Windup")
                    .resolve("windupgraph_classmetadatatest_" + RandomStringUtils.randomAlphanumeric(6));
    }

}
