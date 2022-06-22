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
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.phase.ArchiveExtractionPhase;
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
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@RunWith(Arquillian.class)
public class IdentifyArchivesRulesetTest {
    private static final Path INPUT_PATH = new File("").getAbsoluteFile().toPath().getParent().getParent()
            .resolve("test-files/jee-example-app-1.0.0.ear");
    private static final Path OUTPUT_PATH = Paths.get("target/WindupReport");
    private static final String LOG4J_COORDINATE = "log4j:log4j:4.11";

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-archives"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment() {
        final AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML();

        return archive;
    }

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory contextFactory;

    @Inject
    private CompositeArchiveIdentificationService identifier;

    /**
     * Run initial Windup rules against the JEE sample app, add a single identification record, and check if the lib is identified.
     */
    @Test
    public void testJarsAreIdentified() throws Exception {
        try (GraphContext graphContext = contextFactory.create(true)) {
            FileUtils.deleteDirectory(OUTPUT_PATH.toFile());

            InMemoryArchiveIdentificationService inMemoryIdentifier = new InMemoryArchiveIdentificationService();
            inMemoryIdentifier.addMapping("4bf32b10f459a4ecd4df234ae2ccb32b9d9ba9b7", LOG4J_COORDINATE);
            identifier.addIdentifier(inMemoryIdentifier);

            WindupConfiguration wc = new WindupConfiguration();
            wc.setGraphContext(graphContext);
            wc.addInputPath(INPUT_PATH);
            wc.setOutputDirectory(OUTPUT_PATH);
            wc.setOptionValue(OverwriteOption.NAME, true);
            wc.setRuleProviderFilter(new NotPredicate(
                    new RuleProviderPhasePredicate(ArchiveExtractionPhase.class, DecompilationPhase.class, MigrationRulesPhase.class,
                            ReportGenerationPhase.class, ReportRenderingPhase.class)
            ));

            processor.execute(wc);

            GraphService<IdentifiedArchiveModel> archiveService = new GraphService<>(graphContext, IdentifiedArchiveModel.class);
            Iterable<IdentifiedArchiveModel> archives = archiveService.findAllByProperty(IdentifiedArchiveModel.FILE_NAME, "log4j-1.2.6.jar");
            for (IdentifiedArchiveModel archive : archives) {
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
