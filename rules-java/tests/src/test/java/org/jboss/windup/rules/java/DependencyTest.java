package org.jboss.windup.rules.java;

import org.apache.commons.io.FileUtils;
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
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.model.ProjectDependencyModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.rules.apps.java.condition.Dependency;
import org.jboss.windup.rules.apps.java.condition.Version;
import org.jboss.windup.rules.apps.java.archives.model.ArchiveCoordinateModel;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;
import org.jboss.windup.rules.apps.java.model.JarArchiveModel;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(Arquillian.class)
public class DependencyTest {
    @Inject
    private DependencyTest.TestDependencyProvider provider;
    @Inject
    private WindupProcessor processor;
    @Inject
    private GraphContextFactory factory;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-archives"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClass(DependencyTest.TestDependencyProvider.class);
    }

    @Test
    public void testDependencyWithHint() throws IOException {
        try (GraphContext context = factory.create(true)) {
            ProjectModel projectModel = context.getFramed().addFramedVertex(ProjectModel.class);
            projectModel.setName("Main Project");

            ArchiveModel dependencyArchiveModel = context.getFramed().addFramedVertex(ArchiveModel.class);
            dependencyArchiveModel.setFilePath("src/test/resources/dependencies/spring-boot-starter-logging-1.2.7.RELEASE.jar");

            ArchiveCoordinateModel archiveCoordinateModel = context.getFramed().addFramedVertex(ArchiveCoordinateModel.class);
            archiveCoordinateModel.setGroupId("org.springframework.boot");
            archiveCoordinateModel.setArtifactId("spring-boot-starter-logging");
            archiveCoordinateModel.setVersion("1.2.7.RELEASE");
            IdentifiedArchiveModel dependency = context.service(IdentifiedArchiveModel.class).addTypeToModel(dependencyArchiveModel);
            dependency.setCoordinate(archiveCoordinateModel);
            projectModel.addFileModel(dependency);

            IdentifiedArchiveModel dependencyWithoutCoordinate = context.getFramed().addFramedVertex(IdentifiedArchiveModel.class);
            dependencyWithoutCoordinate.setFilePath("src/test/resources/dependencies/known-package-case.jar");
            projectModel.addFileModel(dependencyWithoutCoordinate);

            ArchiveCoordinateModel tooNewArchiveCoordinateModel = context.getFramed().addFramedVertex(ArchiveCoordinateModel.class);
            tooNewArchiveCoordinateModel.setGroupId("org.springframework.boot");
            tooNewArchiveCoordinateModel.setArtifactId("spring-boot-starter-logging");
            tooNewArchiveCoordinateModel.setVersion("7.2.1.RELEASE");
            IdentifiedArchiveModel tooNewDependency = context.getFramed().addFramedVertex(IdentifiedArchiveModel.class);
            tooNewDependency.setFilePath("src/test/resources/dependencies/spring-boot-starter-logging-7.2.1.RELEASE.jar");
            tooNewDependency.setCoordinate(tooNewArchiveCoordinateModel);
            projectModel.addFileModel(tooNewDependency);

            // this has exactly the same coordinates of the above 'archiveCoordinateModel' so that it can be checked
            // that the same JAR do not trigger twice the dependency condition
            MavenProjectModel dependencyMavenProjectModel = context.getFramed().addFramedVertex(MavenProjectModel.class);
            dependencyMavenProjectModel.setName("Dependency Project");
            dependencyMavenProjectModel.setGroupId("org.springframework.boot");
            dependencyMavenProjectModel.setArtifactId("spring-boot-starter-logging");
            dependencyMavenProjectModel.setVersion("1.2.7.RELEASE");
            JarArchiveModel jarArchiveModel = context.service(JarArchiveModel.class).addTypeToModel(dependencyArchiveModel);
            dependencyMavenProjectModel.addFileModel(jarArchiveModel);
            dependencyMavenProjectModel.setRootFileModel(jarArchiveModel);
            projectModel.addChildProject(dependencyMavenProjectModel);

            MavenProjectModel wrongArtifactIdMavenProjectModel = context.getFramed().addFramedVertex(MavenProjectModel.class);
            wrongArtifactIdMavenProjectModel.setGroupId("org.springframework.boot");
            wrongArtifactIdMavenProjectModel.setArtifactId("spring-boot-starter-web");
            wrongArtifactIdMavenProjectModel.setVersion("1.2.7.RELEASE");
            JarArchiveModel wrongArtifactIdJarArchiveModel = context.getFramed().addFramedVertex(JarArchiveModel.class);
            wrongArtifactIdJarArchiveModel.setFilePath("src/test/resources/dependencies/spring-boot-starter-web-1.2.7.RELEASE.jar");
            wrongArtifactIdMavenProjectModel.addFileModel(wrongArtifactIdJarArchiveModel);
            wrongArtifactIdMavenProjectModel.setRootFileModel(wrongArtifactIdJarArchiveModel);
            projectModel.addChildProject(wrongArtifactIdMavenProjectModel);

            FileModel inputPath = context.getFramed().addFramedVertex(FileModel.class);
            inputPath.setFilePath("src/test/resources/dependencies/");

            Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "windup_"
                    + UUID.randomUUID().toString());
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            projectModel.addFileModel(inputPath);
            projectModel.setRootFileModel(inputPath);

            WindupConfiguration windupConfiguration = new WindupConfiguration()
                    .setGraphContext(context);
            windupConfiguration.addInputPath(Paths.get(inputPath.getFilePath()));
            windupConfiguration.setOutputDirectory(outputPath);
            processor.execute(windupConfiguration);

            Assert.assertEquals(11, provider.getMatches().size());
            Assert.assertEquals(1, provider.getMatches().get(0).getLineNumber());
            Assert.assertEquals(1, provider.getMatches().get(0).getColumnNumber());
            Assert.assertEquals(1, provider.getMatches().get(0).getLength());
            Assert.assertEquals("Dependency Archive Match", provider.getMatches().get(0).getSourceSnippit());
        }
    }

    @Singleton
    public static class TestDependencyProvider extends AbstractRuleProvider {

        private List<FileLocationModel> matches = new ArrayList<>();

        public TestDependencyProvider()
        {
            super(MetadataBuilder.forProvider(DependencyTest.TestDependencyProvider.class).setPhase(MigrationRulesPhase.class));
        }

        public void addMatch(FileLocationModel match)
        {
            this.matches.add(match);
        }

        public List<FileLocationModel> getMatches() {
            return this.matches;
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            AbstractIterationOperation<FileLocationModel> addMatch = new AbstractIterationOperation<FileLocationModel>() {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context, FileLocationModel payload) {
                    addMatch(payload);
                }
            };

            return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(Dependency.withGroupId("org.springframework.boot")
                            .andArtifactId("spring-boot-starter-logging")
                            .andVersion(Version.fromVersion("1.2.3.RELEASE").to("2.1.3.Final")))
                    .perform(Hint.titled("Test Hint").withText("Test Hint Body").withEffort(1).and(addMatch))
                    .addRule()
                    .when(Dependency.withGroupId("org.springframework.{*}")
                            .andArtifactId("spring-boot-starter{artifactName}")
                            .andVersion(Version.fromVersion("1.2.3.RELEASE").to("2.1.3.Final")))
                    .perform(Hint.titled("Test Hint").withText("Test Hint Body").withEffort(2).and(addMatch))
                    .where("artifactName").matches("-logging")
                    .addRule()
                    .when(Dependency.withGroupId("org.springframework.{*}")
                            .andVersion(Version.fromVersion("1.2.3.RELEASE").to("2.1.3.Final")))
                    .perform(Hint.titled("Test Hint").withText("Test Hint Body").withEffort(3).and(addMatch))
                    .addRule()
                    .when(Dependency.withArtifactId("spring-boot-starter{artifactName}")
                            .andVersion(Version.fromVersion("1.2.3.RELEASE").to("2.1.3.Final")))
                    .perform(Hint.titled("Test Hint").withText("Test Hint Body").withEffort(4).and(addMatch))
                    .where("artifactName").matches(".*")
                    .addRule()
                    .when(Dependency.withGroupId("org.springframework.{*}")
                            .andArtifactId("spring-boot-starter{artifactName}"))
                    .perform(Hint.titled("Test Hint").withText("Test Hint Body").withEffort(5).and(addMatch))
                    .where("artifactName").matches(".*")
                    .addRule()
                    .when(Dependency.withVersion(Version.fromVersion("1.2.3.RELEASE").to("2.1.3.Final")))
                    .perform(Hint.titled("Test Hint").withText("Test Hint Body").withEffort(6).and(addMatch))
                    .addRule()
                    .when(Dependency.withGroupId("org.springframework.boot")
                            .andArtifactId("spring-boot-starter-logging")
                            .andVersion(Version.fromVersion("1.0.0").to("1.2.0.RELEASE")))
                    .perform(Hint.titled("Wrong Test Hint").withText("Wrong Test Hint Body").withEffort(7).and(addMatch))
                    .addRule()
                    .when(Dependency.withGroupId("org.springframework.boot")
                            .andArtifactId("spring-boot-starter-logging")
                            .andVersion(Version.fromVersion("2.1.3.Final").to("3.2")))
                    .perform(Hint.titled("Wrong Test Hint").withText("Wrong Test Hint Body").withEffort(8).and(addMatch));
        }
        // @formatter:on
    }

}
