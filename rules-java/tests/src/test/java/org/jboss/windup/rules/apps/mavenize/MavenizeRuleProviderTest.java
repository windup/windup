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
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.archives.identify.CompositeArchiveIdentificationService;
import org.jboss.windup.rules.apps.java.archives.identify.InMemoryArchiveIdentificationService;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import static org.jboss.windup.rules.apps.mavenize.PackagesToContainingMavenArtifactsIndex.EDGE_USES;
import org.jboss.windup.testutil.basics.WindupTestUtilMethods;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@RunWith(Arquillian.class)
public class MavenizeRuleProviderTest
{
    private static final Logger LOG = Logger.getLogger( MavenizeRuleProviderTest.class.getName() );

    @Deployment
    @AddonDependencies({
        @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
        @AddonDependency(name = "org.jboss.windup.config:windup-config"),
        @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
        @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
        @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-base"),
        @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
        @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-archives"),
        //@AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
        //@AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-project"),
        //@AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-tattletale"),
        //@AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-xml"),
        @AddonDependency(name = "org.jboss.windup.tests:test-util"),
        //@AddonDependency(name = "org.jboss.windup:windup-tooling"),
    })
    public static AddonArchive getDeployment()
    {
        // See https://github.com/shrinkwrap/resolver/blob/master/README.asciidoc
        JavaArchive[] archives = Maven.resolver().resolve("org.jboss.windup.maven:nexus-indexer-data:zip:5").withoutTransitivity().as(JavaArchive.class);
        Assert.assertEquals("maven-indexer-data found", 1, archives.length);
        AddonArchive deployment = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML();
        for (JavaArchive archive : archives)
            deployment.merge(archive);
        return deployment;
    }

    @Inject
    MavenizeRuleProvider provider;

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Inject
    private CompositeArchiveIdentificationService identifier;

    @Before
    public void addMavenCoordsData(){
        InMemoryArchiveIdentificationService inMemoryIdentifier = new InMemoryArchiveIdentificationService();
        inMemoryIdentifier.addMapping("4bf32b10f459a4ecd4df234ae2ccb32b9d9ba9b7", "log4j:log4j:1.2.6");
        inMemoryIdentifier.addMapping("b0236b252e86419eef20c31a44579d2aee2f0a69", "commons-lang:commons-lang:2.5");
        identifier.addIdentifier(inMemoryIdentifier);
    }


    @Test
    public void testMavenizeRuleProvider() throws IOException, InstantiationException, IllegalAccessException
    {
        try (GraphContext grCtx = factory.create(WindupTestUtilMethods.getTempDirectoryForGraph()))
        {
            final String inputDir = "../../test-files/jee-example-app-1.0.0.ear"; // rules-java/api
            final Class<MavenizeRuleProvider> ruleToRunUpTo = MavenizeRuleProvider.class;

            final Path outputDir = executeWindupAgainstAppUntilRule(inputDir, grCtx, ruleToRunUpTo);

            Iterable<IdentifiedArchiveModel> identifiedArchives = grCtx.service(IdentifiedArchiveModel.class).findAll();
            Assume.assumeTrue(identifiedArchives.iterator().hasNext());


            // Were the pom.xml's created?
            final Path baseMavenDir = outputDir.resolve("mavenized");

            Path resultRootPomPath = baseMavenDir.resolve("pom.xml");
            Assert.assertTrue("Exists: " + resultRootPomPath, resultRootPomPath.toFile().exists());

            checkPomExistence(baseMavenDir, "jee-example-app-bom", true);
            checkPomExistence(baseMavenDir, "jee-example-ejb-services", true);
            checkPomExistence(baseMavenDir, "log4j", false);
            checkPomExistence(baseMavenDir, "unparsable-jar", false);

            // TODO: Load the POM tree with Maven?
            //ProjectBuilder pb = new DefaultProjectBuilder();
            //pb.build(new ArrayList<File>(){{add(outputDir.toFile());}}, true, new DefaultProjectBuildingRequest());
        }
    }

    private static void checkPomExistence(Path base, String subdirName, boolean shouldExist){
        Path pomPath = base.resolve(subdirName).resolve("pom.xml");
        if (shouldExist)
            Assert.assertTrue("Exists: " + pomPath, pomPath.toFile().exists());
        else
            Assert.assertFalse("Not exists: " + pomPath, pomPath.toFile().exists());
    }


    private Path executeWindupAgainstAppUntilRule(final String inputDir, final GraphContext grCtx, final Class<MavenizeRuleProvider> ruleToRunUpTo) throws IOException, IllegalAccessException, InstantiationException
    {
        Assume.assumeTrue("Exists: " + inputDir, new File(inputDir).exists());

        final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "Windup-Mavenization-output" );
        FileUtils.deleteDirectory(outputPath.toFile());
        Files.createDirectories(outputPath);

        // Simulate the discovered project
        ProjectModel pm = grCtx.getFramed().addVertex(null, ProjectModel.class);
        pm.setName("Main Project");

        // TODO: Add ProjectModel --uses--> ArchiveCoordinateModel to simulate API packages found in Java files.

        FileModel inputPathFrame = grCtx.getFramed().addVertex(null, FileModel.class);
        inputPathFrame.setFilePath(inputDir);
        inputPathFrame.setProjectModel(pm);
        pm.addFileModel(inputPathFrame);
        pm.setRootFileModel(inputPathFrame);

        grCtx.getGraph().getBaseGraph().commit();

        // Configure Windup core
        final WindupConfiguration processorConfig = new WindupConfiguration();
        processorConfig.setRuleProviderFilter(new RuleProviderWithDependenciesPredicate(ruleToRunUpTo));
        processorConfig.setGraphContext(grCtx);
        processorConfig.addInputPath(Paths.get(inputDir));
        processorConfig.setOutputDirectory(outputPath);
        processorConfig.setOffline(true);
        processorConfig.setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""));
        processorConfig.setOptionValue(SourceModeOption.NAME, false);
        processorConfig.setOptionValue(MavenizeOption.NAME, true);

        processor.execute(processorConfig);

        return outputPath;
    }

}
