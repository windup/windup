package org.jboss.windup.rules.apps.java.archives;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.phase.Decompilation;
import org.jboss.windup.config.phase.MigrationRules;
import org.jboss.windup.config.phase.ReportGeneration;
import org.jboss.windup.config.phase.ReportRendering;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.configuration.options.OverwriteOption;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.archives.identify.IdentifiedArchives;
import org.jboss.windup.rules.apps.java.archives.ignore.SkippedArchives;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;
import org.jboss.windup.rules.apps.java.archives.model.IgnoredArchiveModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Ondrej Zizka, ozizka at redhat.com
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@RunWith(Arquillian.class)
public class SkipArchivesRulesetTest
{
    private static final Path INPUT_PATH = new File("").getAbsoluteFile().toPath().getParent().getParent()
                .resolve("test-files/jee-example-app-1.0.0.ear");
    private static final Path OUTPUT_PATH = Paths.get("target/WindupReport");

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.utils:utils"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java-archives"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                                AddonDependencyEntry.create("org.jboss.windup.utils:utils"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java-archives"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory contextFactory;

    @Test
    public void testSkippedArchivesFound() throws Exception
    {
        try (GraphContext graphContext = contextFactory.create())
        {
            FileUtils.deleteDirectory(OUTPUT_PATH.toFile());

            String log4jCoordinate = "log4j:log4j:4.11";
            IdentifiedArchives.addMapping("4bf32b10f459a4ecd4df234ae2ccb32b9d9ba9b7", log4jCoordinate);
            SkippedArchives.add("log4j:*:*");

            WindupConfiguration wc = new WindupConfiguration();
            wc.setGraphContext(graphContext);
            wc.setInputPath(INPUT_PATH);
            wc.setOutputDirectory(OUTPUT_PATH);
            wc.setOptionValue(OverwriteOption.NAME, true);
            wc.setRuleProviderFilter(new Predicate<WindupRuleProvider>()
            {
                @Override
                public boolean accept(WindupRuleProvider type)
                {
                    return !(type.getPhase().isAssignableFrom(ReportGeneration.class))
                                && !(type.getPhase().isAssignableFrom(ReportRendering.class))
                                && !(type.getPhase().isAssignableFrom(Decompilation.class))
                                && !(type.getPhase().isAssignableFrom(MigrationRules.class));
                }
            });

            processor.execute(wc);

            GraphService<IgnoredArchiveModel> archiveService = new GraphService<>(graphContext, IgnoredArchiveModel.class);
            Iterable<IgnoredArchiveModel> archives = archiveService.findAllByProperty(IgnoredArchiveModel.FILE_NAME, "log4j-1.2.6.jar");
            for (IgnoredArchiveModel archive : archives)
            {
                ArchiveCoordinateModel archiveCoordinate = ((IdentifiedArchiveModel) archive).getCoordinate();
                Assert.assertNotNull(archiveCoordinate);

                final Coordinate expected = CoordinateBuilder.create(log4jCoordinate);
                Assert.assertEquals(expected, CoordinateBuilder.create()
                            .setGroupId(archiveCoordinate.getGroupId())
                            .setArtifactId(archiveCoordinate.getArtifactId())
                            .setClassifier(archiveCoordinate.getClassifier())
                            .setVersion(archiveCoordinate.getVersion()));
            }
        }
    }

}
