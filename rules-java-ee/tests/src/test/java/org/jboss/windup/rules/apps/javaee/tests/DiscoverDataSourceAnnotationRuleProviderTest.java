package org.jboss.windup.rules.apps.javaee.tests;

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
import org.jboss.windup.rules.apps.javaee.model.DataSourceModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class DiscoverDataSourceAnnotationRuleProviderTest extends AbstractTest {
    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testDataSourceDiscovery() throws Exception {
        Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "DiscoverDataSourceTest_"
                + UUID.randomUUID().toString());
        FileUtils.deleteDirectory(outputPath.toFile());
        Files.createDirectories(outputPath);

        try (GraphContext context = factory.create(outputPath, true)) {
            ProjectModel pm = context.getFramed().addFramedVertex(ProjectModel.class);
            pm.setName("Main Project");
            FileModel inputPath = context.getFramed().addFramedVertex(FileModel.class);
            inputPath.setFilePath("src/test/resources/discover-data-source");

            pm.addFileModel(inputPath);
            pm.setRootFileModel(inputPath);
            WindupConfiguration windupConfiguration = new WindupConfiguration()
                    .setGraphContext(context);
            windupConfiguration.addInputPath(Paths.get(inputPath.getFilePath()));
            windupConfiguration.setOptionValue(SourceModeOption.NAME, true);
            windupConfiguration.setOutputDirectory(outputPath);
            windupConfiguration.setOptionValue(KeepWorkDirsOption.NAME, true);
            processor.execute(windupConfiguration);


            GraphService<DataSourceModel> service = new GraphService<>(context, DataSourceModel.class);
            boolean resultFound = false;
            for (DataSourceModel model : service.findAllByProperty(DataSourceModel.NAME, "java:comp/env/HelloStatefulEJB_DataSource")) {
                if (model.getXa() != null && model.getXa()) {
                    resultFound = true;
                }
            }

            Assert.assertTrue(resultFound);
        } finally {
            FileUtils.deleteQuietly(outputPath.toFile());
        }
    }
}
