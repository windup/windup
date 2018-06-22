package org.jboss.windup.rules.apps.javaee.tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.javaee.AbstractTest;
import org.jboss.windup.rules.apps.javaee.model.EjbEntityBeanModel;
import org.jboss.windup.rules.apps.javaee.model.EjbMessageDrivenModel;
import org.jboss.windup.rules.apps.javaee.model.EjbSessionBeanModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class DiscoverEjbConfigurationTest extends AbstractTest
{
    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testEJBMessageDrivenNotInEJBXML() throws Exception
    {
        try (GraphContext context = factory.create(true))
        {
            String inputPath = "src/test/resources/ejb/mdb";
            executeAnalysis(context, inputPath);

            GraphService<EjbMessageDrivenModel> ejbMessageDrivenService = new GraphService<>(context, EjbMessageDrivenModel.class);
            int entitiesFound = 0;
            for (EjbMessageDrivenModel messageDrivenModel : ejbMessageDrivenService.findAll())
            {
                Assert.assertEquals("EJBMessageDrivenNotInEJBXML", messageDrivenModel.getEjbClass().getClassName());
                entitiesFound++;
            }
            Assert.assertEquals(1, entitiesFound);
        }
    }

    @Test
    public void testEJBSessionBeanNotInEJBXML() throws Exception
    {
        try (GraphContext context = factory.create(true))
        {
            String inputPath = "src/test/resources/ejb/session";
            executeAnalysis(context, inputPath);

            GraphService<EjbSessionBeanModel> ejbSessionService = new GraphService<>(context, EjbSessionBeanModel.class);
            boolean homeFound = false;
            boolean remoteFound = false;
            boolean sessionBeanFound = false;
            boolean localHomeFound = false;
            boolean localObjectFound = false;
            for (EjbSessionBeanModel sessionBeanModel : ejbSessionService.findAll())
            {
                if (sessionBeanModel.getEjbHome() != null && sessionBeanModel.getEjbHome().getQualifiedName().equals("EJB2SessionHomeNotInEJBXML"))
                    homeFound = true;
                if (sessionBeanModel.getEjbRemote() != null && sessionBeanModel.getEjbRemote().getQualifiedName().equals("EJB2RemoteInterfaceNotInEJBXML"))
                    remoteFound = true;
                if (sessionBeanModel.getEjbClass() != null && sessionBeanModel.getEjbClass().getQualifiedName().equals("EJB2SessionBeanNotInEJBXML"))
                    sessionBeanFound = true;
                if (sessionBeanModel.getEjbLocalHome() != null && sessionBeanModel.getEjbLocalHome().getQualifiedName().equals("EJBLocalHomeNotInEJBXML"))
                    localHomeFound = true;
                if (sessionBeanModel.getEjbLocal() != null && sessionBeanModel.getEjbLocal().getQualifiedName().equals("EJBLocalObjectNotInEJBXML"))
                    localObjectFound = true;
            }
            Assert.assertTrue(homeFound);
            Assert.assertTrue(remoteFound);
            Assert.assertTrue(sessionBeanFound);
            Assert.assertTrue(localHomeFound);
            Assert.assertTrue(localObjectFound);
        }
    }

    @Test
    public void testEJBEntityBeanNotInEJBXML() throws Exception
    {
        try (GraphContext context = factory.create(true))
        {
            String inputPath = "src/test/resources/ejb/entity";
            executeAnalysis(context, inputPath);

            GraphService<EjbEntityBeanModel> ejbEntityService = new GraphService<>(context, EjbEntityBeanModel.class);
            int entitiesFound = 0;
            for (EjbEntityBeanModel entityBeanModel : ejbEntityService.findAll())
            {
                Assert.assertEquals("EJB2EntityNotInEJBXML", entityBeanModel.getEjbClass().getClassName());
                entitiesFound++;
            }
            Assert.assertEquals(1, entitiesFound);
        }
    }

    @Test
    public void testEJBMetadataExtraction() throws Exception
    {
        try (GraphContext context = factory.create(true))
        {
            String inputPath = "src/test/resources/";
            executeAnalysis(context, inputPath);

            GraphService<EjbMessageDrivenModel> messageDrivenService = new GraphService<>(context, EjbMessageDrivenModel.class);
            int msgDrivenFound = 0;
            for (EjbMessageDrivenModel msgDriven : messageDrivenService.findAll())
            {
                Assert.assertEquals("ChatBeanDestination", msgDriven.getDestination().getJndiLocation());
                msgDrivenFound++;
            }
            Assert.assertEquals(1, msgDrivenFound);
        }
    }

    private void executeAnalysis(GraphContext context, String inputPathString) throws IOException
    {
        ProjectModel pm = context.getFramed().addFramedVertex(ProjectModel.class);
        pm.setName("Main Project");
        FileModel inputPath = context.getFramed().addFramedVertex(FileModel.class);
        inputPath.setFilePath(inputPathString);

        Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "Windup").resolve(UUID.randomUUID().toString());
        FileUtils.deleteDirectory(outputPath.toFile());
        Files.createDirectories(outputPath);

        pm.addFileModel(inputPath);
        pm.setRootFileModel(inputPath);
        WindupConfiguration windupConfiguration = new WindupConfiguration()
                .setGraphContext(context);
        windupConfiguration.setOptionValue(SourceModeOption.NAME, true);
        windupConfiguration.addInputPath(Paths.get(inputPath.getFilePath()));
        windupConfiguration.setOutputDirectory(outputPath);
        processor.execute(windupConfiguration);
    }
}
