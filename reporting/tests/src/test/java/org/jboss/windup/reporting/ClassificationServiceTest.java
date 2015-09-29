package org.jboss.windup.reporting;

import java.io.File;
import java.util.Collections;
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
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ClassificationServiceTest
{
    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment()
    {
        AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                    .addBeansXML()
                    .addAsResource(new File("src/test/resources/reports"));
        return archive;
    }

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testHintEffort() throws Exception
    {

        try (GraphContext context = factory.create())
        {
            ClassificationService classificationService = new ClassificationService(context);

            ProjectModel projectModel = fillData(context);
            Set<String> emptySet = Collections.emptySet();
            int totalEffort = classificationService.getMigrationEffortPoints(projectModel, emptySet, emptySet, true);
            Assert.assertEquals(143, totalEffort);

            boolean foundF1Effort = false;
            boolean foundF2Effort = false;
            for (FileModel fm : projectModel.getFileModels())
            {
                if (fm.getFilePath().equals("/f1"))
                {
                    int fileEffort = classificationService.getMigrationEffortPoints(fm);
                    Assert.assertEquals(140, fileEffort);
                    foundF1Effort = true;
                }
                else if (fm.getFilePath().equals("/f2"))
                {
                    int fileEffort = classificationService.getMigrationEffortPoints(fm);
                    Assert.assertEquals(3, fileEffort);
                    foundF2Effort = true;
                }
            }
            Assert.assertTrue(foundF1Effort);
            Assert.assertTrue(foundF2Effort);
        }
    }

    private ProjectModel fillData(GraphContext context)
    {
        ClassificationService classificationService = new ClassificationService(context);

        FileModel f1 = context.getFramed().addVertex(null, FileModel.class);
        f1.setFilePath("/f1");
        FileModel f2 = context.getFramed().addVertex(null, FileModel.class);
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

        ProjectModel projectModel = context.getFramed().addVertex(null, ProjectModel.class);
        projectModel.addFileModel(f1);
        f1.setProjectModel(projectModel);
        projectModel.addFileModel(f2);
        f2.setProjectModel(projectModel);

        return projectModel;
    }

}
