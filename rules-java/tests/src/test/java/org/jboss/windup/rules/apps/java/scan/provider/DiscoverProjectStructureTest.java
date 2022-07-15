package org.jboss.windup.rules.apps.java.scan.provider;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.DefaultEvaluationContext;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.DuplicateArchiveModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.jboss.windup.testutil.basics.WindupTestUtilMethods;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.Rule;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class DiscoverProjectStructureTest {
    @Inject
    DiscoverMavenProjectsRuleProvider discoverMavenProvider;
    @Inject
    DiscoverNonMavenArchiveProjectsRuleProvider discoverNonMavenArchiveProvider;
    @Inject
    DiscoverNonMavenSourceProjectsRuleProvider discoverNonMavenSourceProvider;
    @Inject
    private WindupProcessor processor;
    @Inject
    private GraphContextFactory factory;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.windup.tests:test-util"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    /**
     * Project Structure
     *
     * <pre>
     *  - Root - ArchiveModel (MavenProjectModel)
     *       - ArchiveModel (MavenProjectModel)
     *      - DuplicateArchiveModel - null project
     *          -> ArchiveModel - MavenProjectModel
     * </pre>
     * <p>
     * After the structure rules have been run, it should still retain this basic structure.
     * <p>
     * In the past, a bug existed (WINDUP-2019) that would cause the project for DuplicateArchiveModel to be set incorrectly in this
     * scenario. This test serves as a regration test.
     */
    @Test
    public void testDiscoveryWithIgnoredArchives() throws Exception {
        Path graphPath = null;
        try (GraphContext graphContext = factory.create(true)) {
            graphPath = graphContext.getGraphDirectory();

            ArchiveModel application = createArchiveHierarchy(graphContext);
            WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(graphContext);
            configurationModel.addInputPath(application);

            RuleLoaderContext ruleLoaderContext = new RuleLoaderContext();
            Configuration discoverMavenConfiguration = discoverMavenProvider.getConfiguration(ruleLoaderContext);
            Configuration discoverNonMavenArchiveConfiguration = discoverNonMavenArchiveProvider.getConfiguration(ruleLoaderContext);
            Configuration discoverNonMavenSourceConfiguration = discoverNonMavenSourceProvider.getConfiguration(ruleLoaderContext);

            List<Rule> rules = new ArrayList<>();
            rules.addAll(discoverMavenConfiguration.getRules());
            rules.addAll(discoverNonMavenArchiveConfiguration.getRules());
            rules.addAll(discoverNonMavenSourceConfiguration.getRules());

            GraphRewrite event = new GraphRewrite(graphContext);
            DefaultEvaluationContext evaluationContext = WindupTestUtilMethods.createEvalContext(event);
            for (Rule rule : rules) {
                Variables.instance(event).push();
                if (rule.evaluate(event, evaluationContext))
                    rule.perform(event, evaluationContext);
                Variables.instance(event).pop();
            }

            Assert.assertNotNull(application.getProjectModel());
            Assert.assertTrue(application.getProjectModel() instanceof MavenProjectModel);
            Assert.assertEquals("root_project", application.getProjectModel().getName());

            ArchiveModel child1 = (ArchiveModel) application.getAllFiles().stream().filter(child -> child.getFileName().equals("child1.jar"))
                    .findFirst().get();
            Assert.assertEquals("child1.jar", child1.getFileName());

            ArchiveModel child2 = (ArchiveModel) application.getAllFiles().stream()
                    .filter(child -> child.getFileName().equals("child2_duplicate.jar")).findFirst().get();
            Assert.assertEquals("child2_duplicate.jar", child2.getFileName());
            Assert.assertTrue(child2 instanceof DuplicateArchiveModel);
            DuplicateArchiveModel child2AsDuplicate = (DuplicateArchiveModel) child2;
            Assert.assertNull(child2.getProjectModel());
            Assert.assertNotNull(child2AsDuplicate.getCanonicalArchive());
            Assert.assertNotNull(child2AsDuplicate.getCanonicalArchive().getProjectModel());
        } finally {
            if (graphPath != null)
                FileUtils.deleteDirectory(graphPath.toFile());
        }
    }

    private ArchiveModel createArchiveHierarchy(GraphContext graphContext) {
        GraphService<DuplicateArchiveModel> duplicateArchiveService = new GraphService<>(graphContext, DuplicateArchiveModel.class);
        GraphService<MavenProjectModel> mavenProjectModelService = new GraphService<>(graphContext, MavenProjectModel.class);

        ArchiveModel root = createArchive(graphContext, "root");
        MavenProjectModel rootProject = mavenProjectModelService.create();
        rootProject.setName("root_project");
        rootProject.addFileModel(root);
        rootProject.setRootFileModel(root);

        ArchiveModel child1 = createArchive(graphContext, "child1");
        root.addFileToDirectory(child1);
        MavenProjectModel child1Project = mavenProjectModelService.create();
        rootProject.addChildProject(child1Project);
        child1Project.setName("child1_project");
        child1Project.addFileModel(child1);
        child1Project.setRootFileModel(child1);

        ArchiveModel child2 = createArchive(graphContext, "child2_duplicate");
        root.addFileToDirectory(child2);
        ArchiveModel original = createArchive(graphContext, "child2_canonical");
        MavenProjectModel child2Project = mavenProjectModelService.create();
        child2Project.setName("child2_canonical_project");
        child2Project.addFileModel(original);
        child2Project.setRootFileModel(original);

        DuplicateArchiveModel child2AsDuplicate = duplicateArchiveService.addTypeToModel(child2);
        child2AsDuplicate.setCanonicalArchive(original);

        return root;
    }

    private ArchiveModel createArchive(GraphContext graphContext, String name) {
        GraphService<ArchiveModel> archiveService = new GraphService<>(graphContext, ArchiveModel.class);
        ArchiveModel fileModel = archiveService.create();
        fileModel.setFilePath("/path/to/" + name + ".jar");
        fileModel.setFileName(name + ".jar");
        return fileModel;
    }
}
