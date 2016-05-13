package org.jboss.windup.rules.apps.javaee.tests;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.AbstractTest;
import org.jboss.windup.rules.apps.javaee.model.EjbDeploymentDescriptorModel;
import org.jboss.windup.rules.apps.javaee.model.WebXmlModel;
import org.jboss.windup.rules.apps.javaee.rules.jboss.GenerateJBossEjbDescriptorRuleProvider;
import org.jboss.windup.rules.apps.javaee.rules.jboss.GenerateJBossWebDescriptorRuleProvider;
import org.jboss.windup.testutil.basics.WindupTestUtilMethods;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.Collections;

/**
 *  Tests the {@link GenerateJBossWebDescriptorRuleProvider}
 */
@RunWith(Arquillian.class)
public class GenerateWebDescriptorTest extends AbstractTest
{
    @Inject
    private GraphContextFactory factory;

    @Test
    public void testRuleProviders() throws Exception
    {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder))
        {
            initData(context);
            WindupTestUtilMethods.runOnlyRuleProviders(Collections.singletonList(new GenerateJBossWebDescriptorRuleProvider()), context);
            checkWebXmls(context);
        }
    }

    public void initData(GraphContext context)
    {
        ProjectModel pm1 =context.getFramed().addVertex(null, ProjectModel.class);
        ProjectModel pm2 =context.getFramed().addVertex(null, ProjectModel.class);
        FileModel fm1 =context.getFramed().addVertex(null, FileModel.class);
        FileModel fm2 =context.getFramed().addVertex(null, FileModel.class);

        pm1.addFileModel(fm1);
        pm2.addFileModel(fm2);
        WindupConfigurationModel configurationModel =context.getFramed().addVertex(null, WindupConfigurationModel.class);
        GraphService<ProjectModel> projectModels = new GraphService<>(context, ProjectModel.class);
        GraphService<WebXmlModel> webDescriptors = new GraphService<>(context, WebXmlModel.class);
        for (ProjectModel projectModel : projectModels.findAll())
        {
            WebXmlModel webXmlModel =  webDescriptors.create();
            projectModel.addFileModel(webXmlModel);
        }
        configurationModel.addInputPath(fm1);
        configurationModel.addInputPath(fm2);
    }


    private void checkWebXmls(GraphContext context)
    {
        GraphService<WebXmlModel> webXmls = new GraphService<>(context,WebXmlModel.class);
        for (WebXmlModel webXml : webXmls.findAll())
        {
            Assert.assertTrue(1 >= getIterableSize(webXml.getLinksToTransformedFiles()));
        }
    }

    private int getIterableSize(Iterable<LinkModel> linksToTransformedFiles)
    {
        int resultCount = 0;
        for (LinkModel linksToTransformedFile : linksToTransformedFiles)
        {
            resultCount++;
        }
        return resultCount;
    }

}
