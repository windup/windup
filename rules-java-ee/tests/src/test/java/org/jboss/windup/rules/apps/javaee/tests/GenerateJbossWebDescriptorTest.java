package org.jboss.windup.rules.apps.javaee.tests;

import com.google.common.collect.Iterables;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.AbstractTest;
import org.jboss.windup.rules.apps.javaee.model.WebXmlModel;
import org.jboss.windup.rules.apps.javaee.rules.jboss.GenerateJBossWebDescriptorRuleProvider;
import org.jboss.windup.testutil.basics.WindupTestUtilMethods;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.Collections;

/**
 * Tests the {@link GenerateJBossWebDescriptorRuleProvider}
 */
@RunWith(Arquillian.class)
public class GenerateJbossWebDescriptorTest extends AbstractTest {
    @Inject
    private GraphContextFactory factory;

    @Test
    public void testRuleProviders() throws Exception {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder, true)) {
            initData(context);
            WindupTestUtilMethods.runOnlyRuleProviders(Collections.singletonList(new GenerateJBossWebDescriptorRuleProvider()), context);
            checkWebXmls(context);
        }
    }

    public void initData(GraphContext context) {
        ProjectModel parentProject1 = context.getFramed().addFramedVertex(ProjectModel.class);
        parentProject1.setName("parentProject1");
        FileModel parentFileModel1 = context.getFramed().addFramedVertex(FileModel.class);
        parentProject1.addFileModel(parentFileModel1);

        ProjectModel parentProject2 = context.getFramed().addFramedVertex(ProjectModel.class);
        parentProject2.setName("parentProject2");
        FileModel parentFileModel2 = context.getFramed().addFramedVertex(FileModel.class);
        parentProject2.addFileModel(parentFileModel2);

        ProjectModel pm1 = context.getFramed().addFramedVertex(ProjectModel.class);
        pm1.setName("pm1");
        pm1.setParentProject(parentProject1);

        ProjectModel pm2 = context.getFramed().addFramedVertex(ProjectModel.class);
        pm2.setParentProject(parentProject2);

        FileModel fm1 = context.getFramed().addFramedVertex(FileModel.class);
        FileModel fm2 = context.getFramed().addFramedVertex(FileModel.class);

        pm1.addFileModel(fm1);
        pm2.addFileModel(fm2);
        WindupConfigurationModel configurationModel = context.getFramed().addFramedVertex(WindupConfigurationModel.class);
        GraphService<ProjectModel> projectModels = new GraphService<>(context, ProjectModel.class);
        GraphService<WebXmlModel> webDescriptors = new GraphService<>(context, WebXmlModel.class);
        for (ProjectModel projectModel : projectModels.findAll()) {
            WebXmlModel webXmlModel = webDescriptors.create();
            projectModel.addFileModel(webXmlModel);
        }
        configurationModel.addInputPath(parentFileModel1);
        configurationModel.addInputPath(parentFileModel2);
    }


    private void checkWebXmls(GraphContext context) {
        GraphService<WebXmlModel> webXmls = new GraphService<>(context, WebXmlModel.class);
        for (WebXmlModel webXml : webXmls.findAll()) {
            Assert.assertEquals(1, Iterables.size(webXml.getLinksToTransformedFiles()));
        }
    }
}
