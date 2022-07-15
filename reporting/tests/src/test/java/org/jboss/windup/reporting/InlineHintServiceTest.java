package org.jboss.windup.reporting;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.service.InlineHintService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Iterables;

@RunWith(Arquillian.class)
public class InlineHintServiceTest {
    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addAsResource(new File("src/test/resources/reports"));
        return archive;
    }

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testHintEffort() throws Exception {

        try (GraphContext context = factory.create(true)) {
            InlineHintService inlineHintService = new InlineHintService(context);

            ProjectModel projectModel = fillData(context);
            ProjectModelTraversal projectModelTraversal = new ProjectModelTraversal(projectModel);
            Set<String> emptySet = Collections.emptySet();
            final Map<Integer, Integer> effortByCategory = inlineHintService.getMigrationEffortByPoints(projectModelTraversal, emptySet, emptySet, emptySet, true, true);
            int totalEffort = 0;
            for (Map.Entry<Integer, Integer> effortEntry : effortByCategory.entrySet())
                totalEffort += effortEntry.getKey() * effortEntry.getValue();
            Assert.assertEquals(153, totalEffort);

            boolean foundF1Effort = false;
            boolean foundF2Effort = false;
            for (FileModel fm : projectModel.getFileModels()) {
                if (fm.getFilePath().equals("/f1")) {
                    int fileEffort = inlineHintService.getMigrationEffortPoints(fm);
                    Assert.assertEquals(150, fileEffort);
                    foundF1Effort = true;
                } else if (fm.getFilePath().equals("/f2")) {
                    int fileEffort = inlineHintService.getMigrationEffortPoints(fm);
                    Assert.assertEquals(3, fileEffort);
                    foundF2Effort = true;
                }
            }
            Assert.assertTrue(foundF1Effort);
            Assert.assertTrue(foundF2Effort);
        }
    }

    @Test
    public void testFindHintsForProject() throws Exception {
        try (GraphContext context = factory.create(true)) {
            FileService fileService = new FileService(context);
            InlineHintService hintService = new InlineHintService(context);
            ProjectService projectService = new ProjectService(context);

            ProjectModel parent = projectService.create();
            parent.setName("parent");

            FileModel fileP1 = fileService.create();
            InlineHintModel hintP1 = hintService.create();
            hintP1.setFile(fileP1);

            FileModel fileP2 = fileService.create();
            InlineHintModel hintP2 = hintService.create();
            hintP2.setFile(fileP2);

            ProjectModel child1 = projectService.create();
            child1.setName("child1");
            child1.setParentProject(parent);

            ProjectModel child2 = projectService.create();
            child2.setName("child2");
            child2.setParentProject(parent);

            FileModel child2File1 = fileService.create();
            child2.addFileModel(child2File1);
            InlineHintModel child2HintFile1 = hintService.create();
            child2HintFile1.setFile(child2File1);

            FileModel child2File2 = fileService.create();
            child2.addFileModel(child2File2);
            InlineHintModel child2HintFile2 = hintService.create();
            child2HintFile2.setFile(child2File2);

            ProjectModel child3 = projectService.create();
            child3.setName("child3");
            child3.setParentProject(parent);

            FileModel child3File1 = fileService.create();
            child3.addFileModel(child3File1);
            InlineHintModel child3HintFile1 = hintService.create();
            child3HintFile1.setFile(child3File1);

            ProjectModel child2_1 = projectService.create();
            child2_1.setName("child2_1");
            child2_1.setParentProject(child2);
            ProjectModel child2_2 = projectService.create();
            child2_2.setName("child2_2");
            child2_2.setParentProject(child2);

            ProjectModel child2_1_2 = projectService.create();
            child2_1_2.setName("child2_1_2");
            child2_1_2.setParentProject(child2_1);

            FileModel child2_1File1 = fileService.create();
            child2_1_2.addFileModel(child2_1File1);
            InlineHintModel child2_1HintFile1 = hintService.create();
            child2_1HintFile1.setFile(child2_1File1);

            ProjectModel child2_3 = projectService.create();
            child2_3.setName("child2_3");
            child2_3.setParentProject(child2);

            Iterable<InlineHintModel> hints = hintService.getHintsForProject(child2, true);
            Set<InlineHintModel> hintSet = new HashSet<>();

            System.out.println("1: " + child2HintFile1);
            System.out.println("2: " + child2HintFile2);
            System.out.println("3: " + child2_1HintFile1);

            for (InlineHintModel hint : hints) {
                hintSet.add(hint);
            }

            // make sure there were no duplicates
            Assert.assertEquals(Iterables.size(hints), hintSet.size());

            Assert.assertTrue(hintSet.contains(child2HintFile1));
            Assert.assertTrue(hintSet.contains(child2HintFile2));
            Assert.assertTrue(hintSet.contains(child2_1HintFile1));
            Assert.assertTrue(!hintSet.contains(child3HintFile1));
        }
    }

    private ProjectModel fillData(GraphContext context) {
        InlineHintService inlineHintService = new InlineHintService(context);

        FileModel f1 = context.getFramed().addFramedVertex(FileModel.class);
        f1.setFilePath("/f1");
        FileModel f2 = context.getFramed().addFramedVertex(FileModel.class);
        f2.setFilePath("/f2");

        InlineHintModel b1 = inlineHintService.create();
        InlineHintModel b1b = inlineHintService.create();
        b1.setFile(f1);
        b1.setEffort(50);
        b1b.setFile(f1);
        b1b.setEffort(100);

        InlineHintModel b2 = inlineHintService.create();
        b2.setEffort(3);
        b2.setFile(f2);

        ProjectModel projectModel = context.getFramed().addFramedVertex(ProjectModel.class);
        projectModel.addFileModel(f1);
        projectModel.addFileModel(f2);

        return projectModel;
    }
}
