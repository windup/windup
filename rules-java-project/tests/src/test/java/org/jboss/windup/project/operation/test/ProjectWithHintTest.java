package org.jboss.windup.project.operation.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

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
import org.jboss.windup.config.phase.PostMigrationRulesPhase;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.model.ProjectDependencyModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.project.condition.Artifact;
import org.jboss.windup.project.condition.Project;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class ProjectWithHintTest
{
    @Inject
    private ProjectWithHintTest.TestProjectProvider provider;
    @Inject
    private WindupProcessor processor;
    @Inject
    private GraphContextFactory factory;

    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-project"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap.create(AddonArchive.class)
                    .addBeansXML()
                    .addClass(ProjectWithHintTest.TestProjectProvider.class);
    }

    @Test
    public void testProjectWithHint() throws IOException
    {
        try (GraphContext context = factory.create())
        {
            ProjectModel pm = context.getFramed().addVertex(null, ProjectModel.class);
            pm.setName("Main Project");
            ProjectModel subProject = context.getFramed().addVertex(null, MavenProjectModel.class);
            MavenProjectModel subsubProject = context.getFramed().addVertex(null, MavenProjectModel.class);
            subsubProject.setArtifactId("abc");
            ProjectDependencyModel dependency = context.getFramed().addVertex(null, ProjectDependencyModel.class);
            dependency.setClassifier("abc");
            dependency.setProject(subsubProject);

            FileModel dependencyFile = context.getFramed().addVertex(null, FileModel.class);
            dependencyFile.setFilePath("src/test/resources/xml/project.xml");
            subsubProject.addFileModel(dependencyFile);
            FileLocationModel locationReference = context.getFramed().addVertex(null, FileLocationModel.class);
            locationReference.setLineNumber(3);
            locationReference.setColumnNumber(4);
            locationReference.setLength(5);
            locationReference.setSourceSnippit("snippet");
            locationReference.setFile(dependencyFile);
            dependency.setFileLocationReference(Collections.singleton(locationReference));

            subProject.addDependency(dependency);
            pm.addChildProject(subProject);

            FileModel inputPath = context.getFramed().addVertex(null, FileModel.class);
            inputPath.setFilePath("src/test/resources/");
            FileModel subinputPath = context.getFramed().addVertex(null, FileModel.class);
            subinputPath.setFilePath("src/test/resources/org");
            FileModel subsubinputPath = context.getFramed().addVertex(null, FileModel.class);
            subsubinputPath.setFilePath("src/test/resources/org/jboss");

            Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "windup_"
                        + UUID.randomUUID().toString());
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            pm.addFileModel(inputPath);
            pm.setRootFileModel(inputPath);
            subProject.addFileModel(subinputPath);
            subProject.setRootFileModel(subinputPath);
            subsubProject.addFileModel(subsubinputPath);
            subsubProject.setRootFileModel(subsubinputPath);

            WindupConfiguration windupConfiguration = new WindupConfiguration()
                        .setGraphContext(context);
            windupConfiguration.addInputPath(Paths.get(inputPath.getFilePath()));
            windupConfiguration.setOutputDirectory(outputPath);
            processor.execute(windupConfiguration);

            Assert.assertEquals(1, provider.getMatches().size());
            Assert.assertEquals(3, provider.getMatches().get(0).getLineNumber());
            Assert.assertEquals(4, provider.getMatches().get(0).getColumnNumber());
            Assert.assertEquals(5, provider.getMatches().get(0).getLength());
        }
    }

    @Singleton
    public static class TestProjectProvider extends AbstractRuleProvider
    {

        private List<FileLocationModel> matches = new ArrayList<>();

        public TestProjectProvider()
        {
            super(MetadataBuilder.forProvider(ProjectWithHintTest.TestProjectProvider.class)
                        .setPhase(PostMigrationRulesPhase.class));
        }

        public void addMatch(FileLocationModel match)
        {
            this.matches.add(match);
        }

        public List<FileLocationModel> getMatches()
        {
            return this.matches;
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
        {
            AbstractIterationOperation<FileLocationModel> addMatch = new AbstractIterationOperation<FileLocationModel>()
            {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context, FileLocationModel payload)
                {
                    addMatch(payload);
                }
            };

            return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(Project.dependsOnArtifact(Artifact.withArtifactId("abc")))
                    .perform(Hint.titled("Test Hint").withText("Test Hint Body").withEffort(42).and(addMatch));
        }
        // @formatter:on
    }

}
