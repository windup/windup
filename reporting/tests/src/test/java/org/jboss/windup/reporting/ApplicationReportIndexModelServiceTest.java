package org.jboss.windup.reporting;

import java.io.File;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.dao.ProjectModelService;
import org.jboss.windup.graph.model.ProjectModel;
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
    private ProjectModelService projectModelService;
    @Inject
    private ApplicationReportService applicationReportModelService;
    @Inject
    private ApplicationReportIndexService applicationReportIndexModelService;

    @Test
    public void testGetApplicationReportsForProjectModelSortedByPriority()
    {

        ProjectModel projectModel = projectModelService.create();

        ApplicationReportModel m1 = applicationReportModelService.create();
        m1.setReportName("m1");
        m1.setReportPriority(100);
        ApplicationReportModel m2 = applicationReportModelService.create();
        m2.setReportName("m2");
        m2.setReportPriority(200);
        ApplicationReportModel m3 = applicationReportModelService.create();
        m3.setReportName("m3");
        m3.setReportPriority(300);
        ApplicationReportModel m4 = applicationReportModelService.create();
        m4.setReportName("m4");
        m4.setReportPriority(400);
        ApplicationReportModel m5 = applicationReportModelService.create();
        m5.setReportName("m5");
        m5.setReportPriority(500);

        ApplicationReportIndexModel idx1 = applicationReportIndexModelService.create();
        idx1.addProjectModel(projectModel);

        @SuppressWarnings("unused")
        ApplicationReportIndexModel idx2 = applicationReportIndexModelService.create();

        m1.setProjectModel(projectModel);
        m2.setProjectModel(projectModel);
        m3.setProjectModel(projectModel);
        m4.setProjectModel(projectModel);

        ApplicationReportIndexModel result = applicationReportIndexModelService
                    .getApplicationReportIndexForProjectModel(projectModel);
        Assert.assertNotNull(result);
        Assert.assertEquals(idx1.asVertex().getId(), result.asVertex().getId());
    }
}
