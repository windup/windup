package org.jboss.windup.rules.apps.javaee.tests;

import com.google.common.collect.Iterables;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.AbstractTest;
import org.jboss.windup.rules.apps.javaee.model.EjbDeploymentDescriptorModel;
import org.jboss.windup.rules.apps.javaee.rules.jboss.GenerateJBossEjbDescriptorRuleProvider;
import org.jboss.windup.testutil.basics.WindupTestUtilMethods;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

/**
 * Tests the {@link GenerateJBossEjbDescriptorRuleProvider}
 */
@RunWith(Arquillian.class)
public class GenerateJBossEjbDescriptorTest extends AbstractTest {
    @Inject
    private GraphContextFactory factory;

    @Test
    public void testRuleProviders() throws Exception {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder, true)) {
            initData(context);
            WindupTestUtilMethods.runOnlyRuleProviders(Collections.singletonList(new GenerateJBossEjbDescriptorRuleProvider()), context);
            checkEjbXmls(context);
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
        GraphService<EjbDeploymentDescriptorModel> ejbDescriptors = new GraphService<>(context, EjbDeploymentDescriptorModel.class);
        for (ProjectModel projectModel : Arrays.asList(pm1, pm2)) {
            EjbDeploymentDescriptorModel ejbDescriptor = ejbDescriptors.create();
            projectModel.addFileModel(ejbDescriptor);
        }
        configurationModel.addInputPath(parentFileModel1);
        configurationModel.addInputPath(parentFileModel2);
    }


    private void checkEjbXmls(GraphContext context) {
        GraphService<EjbDeploymentDescriptorModel> ejbDescriptors = new GraphService<>(context, EjbDeploymentDescriptorModel.class);
        for (EjbDeploymentDescriptorModel ejbDesc : ejbDescriptors.findAll()) {
            Iterable<LinkModel> linkModels = ejbDesc.getLinksToTransformedFiles();
            linkModels.forEach(linkModel -> Assert.assertTrue(linkModel.getLink().endsWith("jboss-ejb3.xml")));
            Assert.assertEquals(1, Iterables.size(linkModels));
        }
    }

}
