package org.jboss.windup.rules.apps.mavenize;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.windup.engine.predicates.RuleProviderWithDependenciesPredicate;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.rules.apps.java.archives.identify.CompositeArchiveIdentificationService;
import org.jboss.windup.rules.apps.java.archives.identify.InMemoryArchiveIdentificationService;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.testutil.basics.WindupTestUtilMethods;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@RunWith(Arquillian.class)
public class MavenizeRuleProviderTest {
    private static final Logger LOG = Logger.getLogger(MavenizeRuleProviderTest.class.getName());
    @Inject
    MavenizeRuleProvider provider;
    @Inject
    private WindupProcessor processor;
    @Inject
    private GraphContextFactory factory;
    @Inject
    private CompositeArchiveIdentificationService identifier;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-base"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-archives"),
            @AddonDependency(name = "org.jboss.windup.tests:test-util"),
    })
    public static AddonArchive getDeployment() {
        // See https://github.com/shrinkwrap/resolver/blob/master/README.asciidoc
        JavaArchive[] archives = Maven.resolver().resolve("org.jboss.windup.maven:nexus-indexer-data:zip:5").withoutTransitivity()
                .as(JavaArchive.class);
        Assert.assertEquals("maven-indexer-data found", 1, archives.length);
        AddonArchive deployment = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML();
        for (JavaArchive archive : archives)
            deployment.merge(archive);
        return deployment;
    }

    private static void checkPomExistence(Path base, String subdirName, boolean shouldExist) {
        Path pomPath = base.resolve(subdirName).resolve("pom.xml");
        if (shouldExist)
            Assert.assertTrue("Exists: " + pomPath, pomPath.toFile().exists());
        else
            Assert.assertFalse("Not exists: " + pomPath, pomPath.toFile().exists());
    }

    @Before
    public void addMavenCoordsData() {
        InMemoryArchiveIdentificationService inMemoryIdentifier = new InMemoryArchiveIdentificationService();
        inMemoryIdentifier.addMapping("6660ebb91c2259c15a2db4a8a21c0d65ac39e33c", "log4j:log4j:1.2.6");
        inMemoryIdentifier.addMapping("07df6997525697b211367cf7f359e5232ec65375", "commons-lang:commons-lang:2.5");
        identifier.addIdentifier(inMemoryIdentifier);
    }

    @Test
    public void testMavenizeRuleProvider() throws IOException, InstantiationException, IllegalAccessException {
        try (GraphContext graphContext = factory.create(WindupTestUtilMethods.getTempDirectoryForGraph(), true)) {
            final String inputDir = "../../test-files/jee-example-app-1.0.0.ear"; // rules-java/api
            final Class<MavenizeRuleProvider> ruleToRunUpTo = MavenizeRuleProvider.class;

            final Path outputDir = executeWindupAgainstAppUntilRule(inputDir, graphContext, ruleToRunUpTo);

            Iterable<IdentifiedArchiveModel> identifiedArchives = graphContext.service(IdentifiedArchiveModel.class).findAll();
            Assume.assumeTrue(identifiedArchives.iterator().hasNext());

            // Were the pom.xml's created?
            final Path baseMavenDir = outputDir.resolve("mavenized/jee-example-app");

            Path resultRootPomPath = baseMavenDir.resolve("pom.xml");
            Assert.assertTrue("Exists: " + resultRootPomPath, resultRootPomPath.toFile().exists());

            checkPomExistence(baseMavenDir, "jee-example-app-bom", true);
            checkPomExistence(baseMavenDir, "jee-example-services-jar", true);
            checkPomExistence(baseMavenDir, "jee-example-services", false);
            checkPomExistence(baseMavenDir, "log4j", false);
            checkPomExistence(baseMavenDir, "log4j-jar", false);
            checkPomExistence(baseMavenDir, "unparsable-jar", false);

            // TODO: Load the POM tree with Maven?
            // ProjectBuilder pb = new DefaultProjectBuilder();
            // pb.build(new ArrayList<File>(){{add(outputDir.toFile());}}, true, new DefaultProjectBuildingRequest());
        }
    }

    private Path executeWindupAgainstAppUntilRule(final String inputDir, final GraphContext grCtx, final Class<MavenizeRuleProvider> ruleToRunUpTo)
            throws IOException, IllegalAccessException, InstantiationException {
        Assume.assumeTrue("Exists: " + inputDir, new File(inputDir).exists());

        final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "Windup-Mavenization-output");
        FileUtils.deleteDirectory(outputPath.toFile());
        Files.createDirectories(outputPath);

        grCtx.commit();

        // Configure Windup core
        final WindupConfiguration processorConfig = new WindupConfiguration();
        processorConfig.setRuleProviderFilter(new RuleProviderWithDependenciesPredicate(ruleToRunUpTo));
        processorConfig.setGraphContext(grCtx);
        processorConfig.addInputPath(Paths.get(inputDir));
        processorConfig.setOutputDirectory(outputPath);
        processorConfig.setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""));
        processorConfig.setOptionValue(SourceModeOption.NAME, false);
        processorConfig.setOptionValue(MavenizeOption.NAME, true);

        processor.execute(processorConfig);

        return outputPath;
    }

}
