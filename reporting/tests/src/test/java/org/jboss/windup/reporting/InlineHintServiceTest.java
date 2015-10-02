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
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.service.InlineHintService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class InlineHintServiceTest
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
            InlineHintService inlineHintService = new InlineHintService(context);

            ProjectModel projectModel = fillData(context);
            Set<String> emptySet = Collections.emptySet();
            int totalEffort = inlineHintService.getMigrationEffortPoints(projectModel, emptySet, emptySet, true);
            Assert.assertEquals(153, totalEffort);

            boolean foundF1Effort = false;
            boolean foundF2Effort = false;
            for (FileModel fm : projectModel.getFileModels())
            {
                if (fm.getFilePath().equals("/f1"))
                {
                    int fileEffort = inlineHintService.getMigrationEffortPoints(fm);
                    Assert.assertEquals(150, fileEffort);
                    foundF1Effort = true;
                }
                else if (fm.getFilePath().equals("/f2"))
                {
                    int fileEffort = inlineHintService.getMigrationEffortPoints(fm);
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
        InlineHintService inlineHintService = new InlineHintService(context);

        FileModel f1 = context.getFramed().addVertex(null, FileModel.class);
        f1.setFilePath("/f1");
        FileModel f2 = context.getFramed().addVertex(null, FileModel.class);
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

        ProjectModel projectModel = context.getFramed().addVertex(null, ProjectModel.class);
        projectModel.addFileModel(f1);
        f1.setProjectModel(projectModel);
        projectModel.addFileModel(f2);
        f2.setProjectModel(projectModel);

        return projectModel;
    }
}
