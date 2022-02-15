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
import org.jboss.windup.rules.apps.javaee.model.SpringRemoteServiceModel;
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
            inputPath.setFilePath("src/test/resources/spring/remote-services-xml");

            pm.addFileModel(inputPath);
            pm.setRootFileModel(inputPath);
            WindupConfiguration windupConfiguration = new WindupConfiguration()
                    .setGraphContext(context);
            windupConfiguration.addInputPath(Paths.get(inputPath.getFilePath()));
            windupConfiguration.setOptionValue(SourceModeOption.NAME, true);
            windupConfiguration.setOutputDirectory(outputPath);
            windupConfiguration.setOptionValue(KeepWorkDirsOption.NAME, true);
            processor.execute(windupConfiguration);


            GraphService<SpringRemoteServiceModel> rmiService = new GraphService<>(context, SpringRemoteServiceModel.class);
            Assert.assertTrue(rmiService.findAll().size() == 6);
            Assert.assertTrue(rmiService.findAll().stream().anyMatch(springRemoteServiceModel -> "RMIPOJOImpl".equalsIgnoreCase(springRemoteServiceModel.getImplementationClass().getClassName())));
            Assert.assertTrue(rmiService.findAll().stream().anyMatch(springRemoteServiceModel -> "RMIPOJOInterface".equalsIgnoreCase(springRemoteServiceModel.getInterface().getClassName())));

            Assert.assertTrue(rmiService.findAll().stream().anyMatch(jaxWSWebServiceModel -> "JaxWSPOJOImpl".equalsIgnoreCase(jaxWSWebServiceModel.getImplementationClass().getClassName())));
            Assert.assertTrue(rmiService.findAll().stream().anyMatch(jaxWSWebServiceModel -> "JaxWSPOJOInterface".equalsIgnoreCase(jaxWSWebServiceModel.getInterface().getClassName())));

        } finally
        {
            FileUtils.deleteDirectory(outputPath.toFile());
        }

    }

}
