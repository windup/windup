package org.jboss.windup.reporting.rules.generation.techreport;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.tags.TagServiceHolder;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.DuplicateArchiveModel;
import org.jboss.windup.graph.model.DuplicateProjectModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.TagSetModel;
import org.jboss.windup.reporting.model.TechnologyUsageStatisticsModel;
import org.jboss.windup.reporting.service.TagGraphService;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class TechReportServiceTest {
    @Inject
    private GraphContextFactory factory;
    @Inject
    private TagServiceHolder tagServiceHolder;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML();
    }

    @Test
    public void testTechReportService() throws Exception {
        Path graphPath = null;
        try (GraphContext graphContext = factory.create(true)) {
            graphPath = graphContext.getGraphDirectory();
            final TagGraphService tagService = new TagGraphService(graphContext);
            tagService.feedTheWholeTagStructureToGraph(tagServiceHolder.getTagService());

            ArchiveModel application = createArchiveHierarchy(graphContext);
            TechReportService techReportService = new TechReportService(graphContext);
            TechReportService.TechStatsMatrix techStatsMatrix = techReportService.getTechStatsMap(application.getProjectModel());

            Assert.assertNotNull(techStatsMatrix);

            ArchiveModel child1 = (ArchiveModel) application.getAllFiles().stream().filter(child -> child.getFileName().equals("child1.jar"))
                    .findFirst().get();
            Assert.assertEquals("child1.jar", child1.getFileName());

            ArchiveModel child2 = (ArchiveModel) application.getAllFiles().stream()
                    .filter(child -> child.getFileName().equals("child2_duplicate.jar")).findFirst().get();
            Assert.assertEquals("child2_duplicate.jar", child2.getFileName());

            TechReportService.TechUsageStatSum statSum = techStatsMatrix.get("techrow:embedded", "techbox:security", Long.valueOf(0), "test");
            Assert.assertNotNull(statSum);
            Assert.assertEquals("test", statSum.getName());
            Assert.assertEquals(2, statSum.getOccurrenceCount());

            TechReportService.TechUsageStatSum statSumJavaEE = techStatsMatrix.get("techrow:java-ee", "techbox:ejb", Long.valueOf(0), "mejb");
            Assert.assertNotNull(statSumJavaEE);
            Assert.assertEquals("mejb", statSumJavaEE.getName());
            Assert.assertEquals(1, statSumJavaEE.getOccurrenceCount());

            TechReportService.TechUsageStatSum statSumValidation = techStatsMatrix.get("techrow:java-ee", "techbox:validation", Long.valueOf(0), "beanvalidation");
            Assert.assertNotNull(statSumValidation);
            Assert.assertEquals("beanvalidation", statSumValidation.getName());
            Assert.assertEquals(1, statSumValidation.getOccurrenceCount());

            TechReportService.TechUsageStatSum statSumBinding = techStatsMatrix.get("techrow:java-ee", "techbox:binding", Long.valueOf(0), "jsonb");
            Assert.assertNotNull(statSumBinding);
            Assert.assertEquals("jsonb", statSumBinding.getName());
            Assert.assertEquals(1, statSumBinding.getOccurrenceCount());


        } finally {
            if (graphPath != null)
                FileUtils.deleteDirectory(graphPath.toFile());
        }
    }

    /**
     * Creates a project structure like this:
     *
     * <pre>
     *     - application1
     *      - child1 - Archive
     *          - TechnologyUsageStatisticsModel - count 1
     *          - child2 - DuplicateArchive
     *          - TechnologyUsageStatisticsModel - count 1
     *     - Shared
     *          - Canonical child2
     *          - TechnologyUsageStatisticsModel - count 1
     * </pre>
     *
     * @param graphContext
     * @return
     */
    private ArchiveModel createArchiveHierarchy(GraphContext graphContext) {
        GraphService<DuplicateArchiveModel> duplicateArchiveService = new GraphService<>(graphContext, DuplicateArchiveModel.class);
        GraphService<MavenProjectModel> mavenProjectModelService = new GraphService<>(graphContext, MavenProjectModel.class);
        GraphService<DuplicateProjectModel> duplicateProjectService = new GraphService<>(graphContext, DuplicateProjectModel.class);

        ArchiveModel root = createArchive(graphContext, "application1");
        MavenProjectModel rootProject = mavenProjectModelService.create();
        rootProject.setName("root_project");
        rootProject.addFileModel(root);
        rootProject.setRootFileModel(root);

        ArchiveModel child1 = createArchive(graphContext, "child1");
        root.addFileToDirectory(child1);
        MavenProjectModel child1Project = mavenProjectModelService.create();
        createTechnologyStats(graphContext, "test", child1Project, "Embedded", "Security", "Sustain");
        rootProject.addChildProject(child1Project);
        child1Project.setName("child1_project");
        child1Project.addFileModel(child1);
        child1Project.setRootFileModel(child1);

        ArchiveModel child2 = createArchive(graphContext, "child2_duplicate");
        root.addFileToDirectory(child2);

        ArchiveModel shared = createArchive(graphContext, "shared");
        ArchiveModel original = createArchive(graphContext, "child2_canonical");
        MavenProjectModel child2Project = mavenProjectModelService.create();
        createTechnologyStats(graphContext, "test", child2Project, "Embedded", "Security", "Sustain");
        child2Project.setName("child2_canonical_project");
        child2Project.addFileModel(shared);
        child2Project.setRootFileModel(shared);

        DuplicateArchiveModel child2AsDuplicate = duplicateArchiveService.addTypeToModel(child2);
        child2AsDuplicate.setCanonicalArchive(original);

        DuplicateProjectModel child2DuplicateProject = duplicateProjectService.create();
        rootProject.addChildProject(child2DuplicateProject);
        createTechnologyStats(graphContext, "test", child2DuplicateProject, "Embedded", "Security", "Sustain");
        createTechnologyStats(graphContext, "mejb", child2DuplicateProject, "Java EE", "Bean", "Connect");
        createTechnologyStats(graphContext, "beanvalidation", child2DuplicateProject, "Java EE", "Validation", "Store");
        createTechnologyStats(graphContext, "jsonb", child2DuplicateProject, "Java EE", "Binding", "Connect");
        child2DuplicateProject.setCanonicalProject(child2Project);
        child2DuplicateProject.addFileModel(child2);
        child2DuplicateProject.setRootFileModel(child2);

        return root;
    }

    private TechnologyUsageStatisticsModel createTechnologyStats(GraphContext graphContext, String name, ProjectModel project, String row, String column, String sector) {
        GraphService<TechnologyUsageStatisticsModel> service = new GraphService<>(graphContext, TechnologyUsageStatisticsModel.class);
        TechnologyUsageStatisticsModel model = service.create();
        model.setComputed(new Date());
        model.setName(name);
        TagSetModel tagSetModel = new GraphService<>(graphContext, TagSetModel.class).create();
        Set<String> tagSet = new HashSet<>();
        tagSet.add(sector);
        tagSet.add(column);
        tagSet.add(row);
        tagSetModel.setTags(tagSet);
        model.setTagModel(tagSetModel);
        model.setOccurrenceCount(1);
        model.setProjectModel(project);
        return model;
    }

    private ArchiveModel createArchive(GraphContext graphContext, String name) {
        GraphService<ArchiveModel> archiveService = new GraphService<>(graphContext, ArchiveModel.class);
        ArchiveModel fileModel = archiveService.create();
        fileModel.setFilePath("/test/path/to/" + name + ".jar");
        fileModel.setFileName(name + ".jar");
        return fileModel;
    }
}
