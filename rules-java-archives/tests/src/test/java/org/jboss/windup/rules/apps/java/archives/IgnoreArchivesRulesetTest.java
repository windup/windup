package org.jboss.windup.rules.apps.java.archives;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.phase.DecompilationPhase;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.phase.ReportRenderingPhase;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.configuration.options.OverwriteOption;
import org.jboss.windup.exec.rulefilters.NotPredicate;
import org.jboss.windup.exec.rulefilters.RuleProviderPhasePredicate;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.archives.identify.CompositeArchiveIdentificationService;
import org.jboss.windup.rules.apps.java.archives.identify.InMemoryArchiveIdentificationService;
import org.jboss.windup.rules.apps.java.archives.ignore.SkippedArchives;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;
import org.jboss.windup.graph.model.IgnoredArchiveModel;
import org.jboss.windup.config.AnalyzeKnownLibrariesOption;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
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
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-archives"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML();
    }

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory contextFactory;

    @Inject
    private CompositeArchiveIdentificationService identifier;

    @After
    public void tearDown() {
        SkippedArchives.unload();
    }

    @Test
    public void testSkippedArchivesFound() throws Exception
    {
        try (GraphContext graphContext = contextFactory.create(true))
        {
            FileUtils.deleteDirectory(OUTPUT_PATH.toFile());

            InMemoryArchiveIdentificationService inMemoryIdentifier = new InMemoryArchiveIdentificationService();
            inMemoryIdentifier.addMapping("4bf32b10f459a4ecd4df234ae2ccb32b9d9ba9b7", LOG4J_COORDINATE);

            InMemoryArchiveIdentificationService identificationService = new InMemoryArchiveIdentificationService();
            identificationService.addMappingsFrom(new File("src/test/resources/testArchiveMapping.txt"));

            identifier.addIdentifier(inMemoryIdentifier);
            identifier.addIdentifier(identificationService);

            SkippedArchives.add("log4j:*:*");

            WindupConfiguration config = new WindupConfiguration();
            config.setGraphContext(graphContext);
            config.addInputPath(INPUT_PATH);
            config.setOutputDirectory(OUTPUT_PATH);
            config.setOptionValue(OverwriteOption.NAME, true);
            config.setRuleProviderFilter(new NotPredicate(
                        new RuleProviderPhasePredicate(DecompilationPhase.class, MigrationRulesPhase.class, ReportGenerationPhase.class,
                                    ReportRenderingPhase.class)
                        ));

            processor.execute(config);

            GraphService<IgnoredArchiveModel> archiveService = new GraphService<>(graphContext, IgnoredArchiveModel.class);
            Iterable<IgnoredArchiveModel> archives = archiveService.findAllByProperty(IgnoredArchiveModel.FILE_NAME, "log4j-1.2.6.jar");
            Assert.assertTrue(archives.iterator().hasNext());
            for (IgnoredArchiveModel archive : archives)
            {
                if (archive.isDirectory())
                    continue;
                Assert.assertNotNull(archive);
                Assert.assertTrue(String.format("%s not instance of IdentifiedArchiveModel", archive), archive instanceof IdentifiedArchiveModel);
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

    @Test
    public void testForceAnalysisOfKnownLibrariesScansAllLibraries() throws Exception {
        try (GraphContext graphContext = contextFactory.create(true)) {
            FileUtils.deleteDirectory(OUTPUT_PATH.toFile());

            WindupConfiguration config = new WindupConfiguration();
            config.setGraphContext(graphContext);
            config.addInputPath(INPUT_PATH);
            config.setOutputDirectory(OUTPUT_PATH);
            config.setOptionValue(OverwriteOption.NAME, true);
            config.setOptionValue(AnalyzeKnownLibrariesOption.NAME, true);
            config.setRuleProviderFilter(new NotPredicate(
                    new RuleProviderPhasePredicate(DecompilationPhase.class, MigrationRulesPhase.class, ReportGenerationPhase.class,
                            ReportRenderingPhase.class)
            ));

            processor.execute(config);

            GraphService<IgnoredArchiveModel> archiveService = new GraphService<>(graphContext, IgnoredArchiveModel.class);
            Iterable<IgnoredArchiveModel> archives = archiveService.findAllByProperty(IgnoredArchiveModel.FILE_NAME, "log4j-1.2.6.jar", true);
            Assert.assertFalse(archives.iterator().hasNext());
        }
    }
}
