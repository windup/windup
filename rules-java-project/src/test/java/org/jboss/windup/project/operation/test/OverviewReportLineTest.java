package org.jboss.windup.project.operation.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.phase.PostMigrationRules;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectDependencyModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.project.condition.Artifact;
import org.jboss.windup.project.condition.Project;
import org.jboss.windup.project.operation.LineItem;
import org.jboss.windup.reporting.model.OverviewReportLineMessageModel;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

@RunWith(Arquillian.class)
public class OverviewReportLineTest
{
    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.rexster:rexster", version = "2.0.0-SNAPSHOT"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules:rules-java-project", version = "2.0.0-SNAPSHOT"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClass(TestProjectProvider.class)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                                AddonDependencyEntry.create("org.jboss.windup.rules:rules-java-project"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );

        return archive;
    }

    @Inject
    private TestProjectProvider provider;

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testHintAndClassificationOperation() throws IOException
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
            subProject.addDependency(dependency);
            pm.addChildProject(subProject);
            FileModel inputPath = context.getFramed().addVertex(null, FileModel.class);
            inputPath.setFilePath("src/test/resources/");
            FileModel subinputPath = context.getFramed().addVertex(null, FileModel.class);
            subinputPath.setFilePath("src/test/resources/org");
            FileModel subsubinputPath = context.getFramed().addVertex(null, FileModel.class);
            subinputPath.setFilePath("src/test/resources/org/jboss");

            Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "windup_"
                        + UUID.randomUUID().toString());
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            inputPath.setProjectModel(pm);
            subinputPath.setProjectModel(subProject);
            subsubinputPath.setProjectModel(subsubProject);
            pm.addFileModel(inputPath);
            pm.setRootFileModel(inputPath);
            subProject.addFileModel(subinputPath);
            subProject.setRootFileModel(subinputPath);
            subsubProject.addFileModel(subsubinputPath);
            subsubProject.setRootFileModel(subsubinputPath);

            WindupConfiguration windupConfiguration = new WindupConfiguration()
                        .setGraphContext(context);
            windupConfiguration.setInputPath(Paths.get(inputPath.getFilePath()));
            windupConfiguration.setOutputDirectory(outputPath);
            processor.execute(windupConfiguration);

            GraphService<OverviewReportLineMessageModel> overviewLineService = new GraphService<>(context, OverviewReportLineMessageModel.class);
            Iterable<OverviewReportLineMessageModel> allOverviewLines = overviewLineService.findAll();
            long count = overviewLineService.count(allOverviewLines);
            Assert.assertEquals(1, count);
            for (OverviewReportLineMessageModel line : allOverviewLines)
            {
                Assert.assertEquals("Just some test message", line.getMessage());
            }
            Assert.assertEquals(1, provider.getMatchCount());
        }
    }

    @Singleton
    public static class TestProjectProvider extends WindupRuleProvider
    {

        private int matchCount;

        @Override
        public Class<? extends RulePhase> getPhase()
        {
            return PostMigrationRules.class;
        }

        public void addMatchCount()
        {
            matchCount++;
        }

        public int getMatchCount()
        {
            return matchCount;
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            
            
            AbstractIterationOperation<ProjectModel> addMatch = new AbstractIterationOperation<ProjectModel>()
            {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context, ProjectModel payload)
                {
                    addMatchCount();
                }
            };

            return ConfigurationBuilder
                        .begin()
                        .addRule()
                        .when(Project.dependsOnArtifact(Artifact.withArtifactId("abc")))
                        .perform(LineItem.withMessage("Just some test message").and(addMatch));
        }
        // @formatter:on
    }

}
