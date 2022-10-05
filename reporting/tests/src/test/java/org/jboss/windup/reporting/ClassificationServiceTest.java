package org.jboss.windup.reporting;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.Lists;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ClassificationServiceTest {
    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addAsResource(new File("src/test/resources/reports"));
    }

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testClassificationEffort() throws Exception {
        try (GraphContext context = factory.create(true)) {
            ClassificationService classificationService = new ClassificationService(context);

            ProjectModel projectModel = fillData(context)[0];
            ProjectModelTraversal projectModelTraversal = new ProjectModelTraversal(projectModel);
            Set<String> emptySet = Collections.emptySet();
            final Map<Integer, Integer> effortByCategory = classificationService.getMigrationEffortByPoints(projectModelTraversal, emptySet, emptySet, emptySet, true,
                    true);
            int totalEffort = 0;
            for (Map.Entry<Integer, Integer> effortEntry : effortByCategory.entrySet())
                totalEffort += effortEntry.getKey() * effortEntry.getValue();

            Assert.assertEquals(143, totalEffort);

            boolean foundF1Effort = false;
            boolean foundF2Effort = false;
            for (FileModel fm : projectModel.getFileModels()) {
                if (fm.getFilePath().equals("/f1")) {
                    int fileEffort = classificationService.getMigrationEffortPoints(fm);
                    Assert.assertEquals(140, fileEffort);
                    foundF1Effort = true;
                } else if (fm.getFilePath().equals("/f2")) {
                    int fileEffort = classificationService.getMigrationEffortPoints(fm);
                    Assert.assertEquals(3, fileEffort);
                    foundF2Effort = true;
                }
            }
            Assert.assertTrue(foundF1Effort);
            Assert.assertTrue(foundF2Effort);
        }
    }

    /**
     * This tests covers the case where a single {@link ClassificationModel} crosses more than one project boundary.
     */
    @Test
    public void testClassificationAcrossProjectBoundaries() throws Exception {
        try (GraphContext context = factory.create(true)) {
            ClassificationService classificationService = new ClassificationService(context);

            ProjectModel projectModel = fillData(context)[1];

            ProjectModelTraversal projectModelTraversal = new ProjectModelTraversal(projectModel);
            Set<String> emptySet = Collections.emptySet();
            final Map<Integer, Integer> effortByCategory = classificationService.getMigrationEffortByPoints(projectModelTraversal, emptySet, emptySet, emptySet, true,
                    true);
            int totalEffort = 0;
            for (Map.Entry<Integer, Integer> effortEntry : effortByCategory.entrySet())
                totalEffort += effortEntry.getKey() * effortEntry.getValue();

            Assert.assertEquals(3, totalEffort);
        }
    }

    @Test
    public void testClassificationAlreadyAttached() throws Exception {
        try (GraphContext context = factory.create(true)) {
            GraphRewrite event = new GraphRewrite(context);
            ClassificationService classificationService = new ClassificationService(context);
            FileService fileService = new FileService(context);
            FileModel file1 = fileService.createByFilePath("/fakepath1");
            FileModel file2 = fileService.createByFilePath("/fakepath2");

            ClassificationModel classificationModel = classificationService.create();
            classificationModel.setClassification("Sample Classification");
            classificationModel.setDescription("Desc");
            classificationModel.setEffort(0);

            classificationModel.addFileModel(file1);

            Assert.assertNotNull(classificationModel.getFileModels());
            Assert.assertEquals(1, Lists.toList(classificationModel.getFileModels()).size());

            classificationService.attachClassification(event, classificationModel, file2);
            Assert.assertEquals(2, Lists.toList(classificationModel.getFileModels()).size());

            classificationService.attachClassification(event, classificationModel, file1);
            Assert.assertEquals(2, Lists.toList(classificationModel.getFileModels()).size());
        }
    }

    private ProjectModel[] fillData(GraphContext context) {
        ClassificationService classificationService = new ClassificationService(context);
        FileService fileService = new FileService(context);
        ProjectService projectService = new ProjectService(context);

        FileModel f1 = fileService.create();
        f1.setFilePath("/f1");
        FileModel f2 = fileService.create();
        f2.setFilePath("/f2");

        ClassificationModel b1 = classificationService.create();
        ClassificationModel b1b = classificationService.create();
        b1.addFileModel(f1);
        b1.setEffort(20);
        b1b.addFileModel(f1);
        b1b.setEffort(120);

        ClassificationModel b2 = classificationService.create();
        b2.addFileModel(f2);
        b2.setEffort(3);

        ProjectModel projectModel = projectService.create();
        projectModel.addFileModel(f1);
        projectModel.addFileModel(f2);

        ProjectModel projectModel2 = projectService.create();
        FileModel f3 = fileService.create();
        f3.setFilePath("/f3");
        projectModel2.addFileModel(f3);
        b2.addFileModel(f3);

        return new ProjectModel[]{projectModel, projectModel2};
    }

}
