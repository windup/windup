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
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.javaee.AbstractTest;
import org.jboss.windup.rules.apps.javaee.model.SpringRestWebServiceModel;
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
public class DiscoverSpringRestServicesRuleProviderTest extends AbstractTest {
    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testSpringRestServiceDiscover() throws IOException {
        Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "DiscoverSpringRestRemoteServicesTest_"
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


            GraphService<SpringRestWebServiceModel> restService = new GraphService<>(context, SpringRestWebServiceModel.class);
            Assert.assertEquals(5, restService.findAll().size());
            Assert.assertTrue(restService.findAll().stream().anyMatch(restModel -> restModel.getPath().contains("/employeesGET") && restModel.getPath().contains("/othersGET")));
            Assert.assertTrue(restService.findAll().stream().anyMatch(restModel -> "/employeesPOST".equalsIgnoreCase(restModel.getPath())));
            Assert.assertTrue(restService.findAll().stream().anyMatch(restModel -> "/employees/{id}/PUT".equalsIgnoreCase(restModel.getPath())));
            Assert.assertTrue(restService.findAll().stream().anyMatch(restModel -> "/employees/{id}/DELETE".equalsIgnoreCase(restModel.getPath())));
            Assert.assertTrue(restService.findAll().stream().anyMatch(restModel -> "/employees/{id}/GET".equalsIgnoreCase(restModel.getPath())));

            TechnologyTagService service = new TechnologyTagService(context);
            Assert.assertTrue(service.findAll().stream().anyMatch(technologyTagModel -> "spring-rest".equalsIgnoreCase(technologyTagModel.getName())));
        } finally
        {
            FileUtils.deleteDirectory(outputPath.toFile());
        }
    }

}
