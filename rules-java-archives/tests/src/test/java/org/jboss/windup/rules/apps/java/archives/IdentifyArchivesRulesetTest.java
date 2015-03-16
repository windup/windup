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
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.phase.ArchiveExtractionPhase;
import org.jboss.windup.config.phase.DecompilationPhase;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.phase.ReportRenderingPhase;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.configuration.options.OverwriteOption;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.archives.identify.CompositeChecksumIdentifier;
import org.jboss.windup.rules.apps.java.archives.identify.InMemoryChecksumIdentifier;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Ondrej Zizka, ozizka at redhat.com
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@RunWith(Arquillian.class)
public class IdentifyArchivesRulesetTest
{
    private static final Path INPUT_PATH = new File("").getAbsoluteFile().toPath().getParent().getParent()
                .resolve("test-files/jee-example-app-1.0.0.ear");
    private static final Path OUTPUT_PATH = Paths.get("target/WindupReport");
    private static final String LOG4J_COORDINATE = "log4j:log4j:4.11";

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-archives"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                                AddonDependencyEntry.create("org.jboss.windup.utils:windup-utils"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:windup-rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:windup-rules-java-archives"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );

        return archive;
    }

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory contextFactory;

    @Inject
    private CompositeChecksumIdentifier identifier;

    @Test
    public void testJarsAreIdentified() throws Exception
    {
        try (GraphContext graphContext = contextFactory.create())
        {
            FileUtils.deleteDirectory(OUTPUT_PATH.toFile());

            InMemoryChecksumIdentifier inMemoryIdentifier = new InMemoryChecksumIdentifier();
            inMemoryIdentifier.addMapping("4bf32b10f459a4ecd4df234ae2ccb32b9d9ba9b7", LOG4J_COORDINATE);
            identifier.addIdentifier(inMemoryIdentifier);

            WindupConfiguration wc = new WindupConfiguration();
            wc.setGraphContext(graphContext);
            wc.setInputPath(INPUT_PATH);
            wc.setOutputDirectory(OUTPUT_PATH);
            wc.setOptionValue(OverwriteOption.NAME, true);
            wc.setRuleProviderFilter(new Predicate<RuleProvider>()
            {
                @Override
                public boolean accept(RuleProvider type)
                {
                    return !(type.getMetadata().getPhase().isAssignableFrom(ReportGenerationPhase.class))
                                && !(type.getMetadata().getPhase().isAssignableFrom(ReportRenderingPhase.class))
                                && !(type.getMetadata().getPhase().isAssignableFrom(DecompilationPhase.class))
                                && !(type.getMetadata().getPhase().isAssignableFrom(ArchiveExtractionPhase.class))
                                && !(type.getMetadata().getPhase().isAssignableFrom(MigrationRulesPhase.class));
                }
            });

            processor.execute(wc);

            GraphService<IdentifiedArchiveModel> archiveService = new GraphService<>(graphContext, IdentifiedArchiveModel.class);
            Iterable<IdentifiedArchiveModel> archives = archiveService.findAllByProperty(IdentifiedArchiveModel.FILE_NAME, "log4j-1.2.6.jar");
            for (IdentifiedArchiveModel archive : archives)
            {
                ArchiveCoordinateModel archiveCoordinate = archive.getCoordinate();
                Assert.assertNotNull(archiveCoordinate);

                final Coordinate expected = CoordinateBuilder.create(LOG4J_COORDINATE);
                Assert.assertEquals(expected, CoordinateBuilder.create()
                            .setGroupId(archiveCoordinate.getGroupId())
                            .setArtifactId(archiveCoordinate.getArtifactId())
                            .setClassifier(archiveCoordinate.getClassifier())
                            .setVersion(archiveCoordinate.getVersion()));
            }
        }
    }

}
