package org.jboss.windup.rules.apps.javaee.tests;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.javaee.model.DataSourceModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test parsing Spring / Hibernate information in order to type JNDI references
 * au
 */
@RunWith(Arquillian.class)
public class SpringDataSourceExtractionTest
{

    @AddonDependencies
    @Deployment
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    private static String SPRING_XMLS = "../../test-files/spring-hibernate-jndi-test";

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;


    @Test
    public void testSpringBeans() throws Exception
    {
        try (GraphContext context = factory.create())
        {
            startWindup(SPRING_XMLS, context);
            GraphService<DataSourceModel> dataSourceService = new GraphService<>(context, DataSourceModel.class);

            int countDataSources = 0;
            //validate all have a datasource type
            for(DataSourceModel model : dataSourceService.findAll()) {
                countDataSources++;
                
                String type = model.getDatabaseTypeName();
                Assert.assertTrue(StringUtils.isNotBlank(type));
                
            }
            Assert.assertEquals(countDataSources, 9);
        }
    }

    private void startWindup(String xmlFilePath, GraphContext context) throws IOException
    {
        ProjectModel pm = context.getFramed().addVertex(null, ProjectModel.class);
        pm.setName("Main Project");
        FileModel inputPath = context.getFramed().addVertex(null, FileModel.class);
        inputPath.setFilePath(xmlFilePath);

        Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "windup_"
                    + UUID.randomUUID().toString());
        FileUtils.deleteDirectory(outputPath.toFile());
        Files.createDirectories(outputPath);

        inputPath.setProjectModel(pm);
        pm.setRootFileModel(inputPath);
        WindupConfiguration windupConfiguration = new WindupConfiguration()
                    .setGraphContext(context);
        windupConfiguration.setInputPath(Paths.get(inputPath.getFilePath()));
        windupConfiguration.setOutputDirectory(outputPath);
        processor.execute(windupConfiguration);
    }
}