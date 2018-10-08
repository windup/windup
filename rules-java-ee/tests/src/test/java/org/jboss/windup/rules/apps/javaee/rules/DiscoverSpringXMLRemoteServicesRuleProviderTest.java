package org.jboss.windup.rules.apps.javaee.rules;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.windup.config.KeepWorkDirsOption;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.javaee.AbstractTest;
import org.jboss.windup.rules.apps.javaee.model.JaxWSWebServiceModel;
import org.jboss.windup.rules.apps.javaee.model.RMIServiceModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RunWith(Arquillian.class)
public class DiscoverSpringXMLRemoteServicesRuleProviderTest extends AbstractTest {
    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testSpringRMIHttpHessianServiceDiscovery() throws IOException {
        Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "DiscoverXMLSpringRemoteServicesTest_"
                + UUID.randomUUID().toString());
        FileUtils.deleteDirectory(outputPath.toFile());
        Files.createDirectories(outputPath);

        try (GraphContext context = factory.create(outputPath, true))
        {
            ProjectModel pm = context.getFramed().addFramedVertex(ProjectModel.class);
            pm.setName("Main Project");
            FileModel inputPath = context.getFramed().addFramedVertex(FileModel.class);
            inputPath.setFilePath("src/test/resources/discover-spring-remote-services");

            pm.addFileModel(inputPath);
            pm.setRootFileModel(inputPath);
            WindupConfiguration windupConfiguration = new WindupConfiguration()
                    .setGraphContext(context);
            windupConfiguration.addInputPath(Paths.get(inputPath.getFilePath()));
            windupConfiguration.setOptionValue(SourceModeOption.NAME, true);
            windupConfiguration.setOutputDirectory(outputPath);
            windupConfiguration.setOptionValue(KeepWorkDirsOption.NAME, true);
            processor.execute(windupConfiguration);


            GraphService<RMIServiceModel> rmiService = new GraphService<>(context, RMIServiceModel.class);
            Assert.assertTrue(rmiService.findAll().size() > 0);
            Assert.assertTrue("RMIPOJOImpl".equalsIgnoreCase(rmiService.findAll().get(0).getImplementationClass().getClassName()));
            Assert.assertTrue("RMIPOJOInterface".equalsIgnoreCase(rmiService.findAll().get(0).getInterface().getClassName()));

            GraphService<JaxWSWebServiceModel> jaxwsService = new GraphService<>(context, JaxWSWebServiceModel.class);
            Assert.assertTrue(jaxwsService.findAll().size() > 0);
            Assert.assertTrue("JaxWSPOJOImpl".equalsIgnoreCase(jaxwsService.findAll().get(0).getImplementationClass().getClassName()));
            Assert.assertTrue("JaxWSPOJOInterface".equalsIgnoreCase(jaxwsService.findAll().get(0).getInterface().getClassName()));

        } finally
        {
            FileUtils.deleteDirectory(outputPath.toFile());
        }

    }

}
