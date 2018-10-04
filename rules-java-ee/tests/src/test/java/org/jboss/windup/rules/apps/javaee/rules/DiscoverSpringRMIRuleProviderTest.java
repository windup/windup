package org.jboss.windup.rules.apps.javaee.rules;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
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
import org.jboss.windup.rules.apps.javaee.model.DataSourceModel;
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

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class DiscoverSpringRMIRuleProviderTest extends AbstractTest {
    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testSpringRMIServiceDiscovery() throws IOException {
        Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "DiscoverSpringRMITest_"
                + UUID.randomUUID().toString());
        FileUtils.deleteDirectory(outputPath.toFile());
        Files.createDirectories(outputPath);

        try (GraphContext context = factory.create(outputPath, true))
        {
            ProjectModel pm = context.getFramed().addFramedVertex(ProjectModel.class);
            pm.setName("Main Project");
            FileModel inputPath = context.getFramed().addFramedVertex(FileModel.class);
            inputPath.setFilePath("src/test/resources/discover-spring-rmi");

            pm.addFileModel(inputPath);
            pm.setRootFileModel(inputPath);
            WindupConfiguration windupConfiguration = new WindupConfiguration()
                    .setGraphContext(context);
            windupConfiguration.addInputPath(Paths.get(inputPath.getFilePath()));
            windupConfiguration.setOptionValue(SourceModeOption.NAME, true);
            windupConfiguration.setOutputDirectory(outputPath);
            windupConfiguration.setOptionValue(KeepWorkDirsOption.NAME, true);
            processor.execute(windupConfiguration);


            GraphService<RMIServiceModel> service = new GraphService<>(context, RMIServiceModel.class);

//            boolean resultFound = false;
//            for (RMIServiceModel model : service.findAllByProperty(DataSourceModel.NAME, "java:comp/env/HelloStatefulEJB_DataSource"))
//            {
//                if (model.getXa() != null && model.getXa())
//                {
//                    resultFound = true;
//                }
//            }

            Assert.assertTrue(service.findAll().size() > 0);
        } finally
        {
            FileUtils.deleteDirectory(outputPath.toFile());
        }

    }
}
