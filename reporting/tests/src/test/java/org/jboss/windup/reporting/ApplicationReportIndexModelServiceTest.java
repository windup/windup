package org.jboss.windup.reporting;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.ApplicationReportIndexModel;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ApplicationReportIndexService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ApplicationReportIndexModelServiceTest
{

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsResource(new File("src/test/resources/reports"))
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testGetApplicationReportsForProjectModelSortedByPriority() throws IOException
    {
        try (GraphContext context = factory.create())
        {
            ProjectModel projectModel = new ProjectService(context).create();
            ApplicationReportService applicationReportService = new ApplicationReportService(context);

            ApplicationReportModel m1 = applicationReportService.create();
            m1.setReportName("m1");
            m1.setReportPriority(100);
            ApplicationReportModel m2 = applicationReportService.create();
            m2.setReportName("m2");
            m2.setReportPriority(200);
            ApplicationReportModel m3 = applicationReportService.create();
            m3.setReportName("m3");
            m3.setReportPriority(300);
            ApplicationReportModel m4 = applicationReportService.create();
            m4.setReportName("m4");
            m4.setReportPriority(400);
            ApplicationReportModel m5 = applicationReportService.create();
            m5.setReportName("m5");
            m5.setReportPriority(500);

            ApplicationReportIndexService applicationReportIndexService = new ApplicationReportIndexService(context);
            ApplicationReportIndexModel idx1 = applicationReportIndexService.create();
            idx1.addProjectModel(projectModel);

            @SuppressWarnings("unused")
            ApplicationReportIndexModel idx2 = applicationReportIndexService.create();

            m1.setProjectModel(projectModel);
            m2.setProjectModel(projectModel);
            m3.setProjectModel(projectModel);
            m4.setProjectModel(projectModel);

            ApplicationReportIndexModel result = applicationReportIndexService
                        .getApplicationReportIndexForProjectModel(projectModel);
            Assert.assertNotNull(result);
            Assert.assertEquals(idx1.asVertex().getId(), result.asVertex().getId());
        }
    }
}
