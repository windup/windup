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
import org.jboss.windup.engine.predicates.RuleProviderWithDependenciesPredicate;
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
import org.jboss.windup.rules.apps.java.archives.config.ArchiveIdentificationConfigLoadingRuleProvider;
import org.jboss.windup.rules.apps.java.archives.config.IgnoredArchivesConfigLoadingRuleProvider;
import org.jboss.windup.rules.apps.java.archives.identify.CompositeChecksumIdentifier;
import org.jboss.windup.rules.apps.java.archives.identify.InMemoryChecksumIdentifier;
import org.jboss.windup.rules.apps.java.archives.identify.SortedFileChecksumIdentifier;
import org.jboss.windup.rules.apps.java.archives.ignore.SkippedArchives;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;
import org.jboss.windup.rules.apps.java.archives.model.IgnoredArchiveModel;
import org.jboss.windup.rules.apps.java.scan.provider.DiscoverFilesAndTypesRuleProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Ondrej Zizka, ozizka at redhat.com
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@RunWith(Arquillian.class)
public class IgnoreArchivesRulesetTest
{
    private static final Path INPUT_PATH = new File("").getAbsoluteFile().toPath().getParent().getParent()
                .resolve("test-files/jee-example-app-1.0.0.ear");
    private static final Path OUTPUT_PATH = Paths.get("target/WindupReport");
    public static final String LOG4J_COORDINATE = "log4j:log4j:::1.2.6";

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.utils:utils"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-archives"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
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
    public void testSkippedArchivesFound() throws Exception
    {
        try (GraphContext graphContext = contextFactory.create())
        {
            FileUtils.deleteDirectory(OUTPUT_PATH.toFile());

            InMemoryChecksumIdentifier inMemoryIdentifier = new InMemoryChecksumIdentifier();
            inMemoryIdentifier.addMapping("4bf32b10f459a4ecd4df234ae2ccb32b9d9ba9b7", LOG4J_COORDINATE);

            SortedFileChecksumIdentifier sortedFileIdentifier = new SortedFileChecksumIdentifier(
                        new File("src/test/resources/testArchiveMapping.txt"));

            identifier.addIdentifier(inMemoryIdentifier);
            identifier.addIdentifier(sortedFileIdentifier);

            SkippedArchives.add("log4j:*:*");

            WindupConfiguration config = new WindupConfiguration();
            config.setGraphContext(graphContext);
            config.setInputPath(INPUT_PATH);
            config.setOutputDirectory(OUTPUT_PATH);
            config.setOptionValue(OverwriteOption.NAME, true);
            config.setRuleProviderFilter(new Predicate<WindupRuleProvider>()
            {
                private RuleProviderWithDependenciesPredicate discoverRuleDeps =
                            new RuleProviderWithDependenciesPredicate(DiscoverFilesAndTypesRuleProvider.class);

                @Override
                public boolean accept(WindupRuleProvider rules)
                {
<<<<<<< HEAD:rules-java-archives/tests/src/test/java/org/jboss/windup/rules/apps/java/archives/IgnoreArchivesRulesetTest.java
                    return (rules instanceof ArchiveIdentificationConfigLoadingRuleProvider
                                || rules instanceof IgnoredArchivesConfigLoadingRuleProvider
                                || discoverRuleDeps.accept(rules))
                                && !rules.getPhase().isAssignableFrom(Decompilation.class);
=======
                    return !(type.getPhase().isAssignableFrom(ReportGenerationPhase.class))
                                && !(type.getPhase().isAssignableFrom(ReportRenderingPhase.class))
                                && !(type.getPhase().isAssignableFrom(DecompilationPhase.class))
                                && !(type.getPhase().isAssignableFrom(MigrationRulesPhase.class));
>>>>>>> 4b112c7ada1299b60ed226750b566afe30e1aea2:rules-java-archives/tests/src/test/java/org/jboss/windup/rules/apps/java/archives/SkipArchivesRulesetTest.java
                }
            });

            processor.execute(config);

            GraphService<IgnoredArchiveModel> archiveService = new GraphService<>(graphContext, IgnoredArchiveModel.class);
            Iterable<IgnoredArchiveModel> archives = archiveService.findAllByProperty(IgnoredArchiveModel.FILE_NAME, "log4j-1.2.6.jar");
            Assert.assertTrue(archives.iterator().hasNext());
            for (IgnoredArchiveModel archive : archives)
            {
                Assert.assertNotNull(archive);
                Assert.assertTrue(archive instanceof IdentifiedArchiveModel);
                ArchiveCoordinateModel archiveCoordinate = ((IdentifiedArchiveModel) archive).getCoordinate();
                Assert.assertNotNull(archiveCoordinate);

                final Coordinate expected = CoordinateBuilder.create(LOG4J_COORDINATE);
                final CoordinateBuilder actual = CoordinateBuilder.create()
                            .setGroupId(archiveCoordinate.getGroupId())
                            .setArtifactId(archiveCoordinate.getArtifactId())
                            .setPackaging(archiveCoordinate.getPackaging())
                            .setClassifier(archiveCoordinate.getClassifier())
                            .setVersion(archiveCoordinate.getVersion());

                Assert.assertEquals(expected.toString(), actual.toString());
            }
        }
    }

}
