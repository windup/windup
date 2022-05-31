/**
 *
 */
package org.jboss.windup.rules.apps.javaee.tests;

import com.google.common.collect.Iterables;
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
import org.jboss.windup.rules.apps.javaee.model.EjbSessionBeanModel;
import org.jboss.windup.rules.apps.javaee.model.JNDIResourceModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

/**
 * Verifies that ResolveJBossLegacyEjbXmlRuleProvider identifies correctly the JNDI NAME element
 * in jboss.xml file
 *
 * @author mnovotny
 *
 */
@RunWith(Arquillian.class)
public class ResolveJBossLegacyEjbXmlRuleProviderTest extends AbstractTest {
    private final static String JNDI_NAME = "THIS_JNDI_NAME_SHOULD_BE_DETECTED";
    private final static String EJB_NAME = "SomeFancyEjb";
    @Inject
    private WindupProcessor processor;
    @Inject
    private GraphContextFactory factory;

    @Test
    public void testJndiName() throws Exception {
        Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "ResolveJBossLegacyEjbXmlRuleProviderTest_"
                + UUID.randomUUID().toString());
        FileUtils.deleteDirectory(outputPath.toFile());
        Files.createDirectories(outputPath);

        try (GraphContext context = factory.create(outputPath, true)) {
            ProjectModel pm = context.getFramed().addFramedVertex(ProjectModel.class);
            pm.setName("Main Project");
            FileModel inputPath = context.getFramed().addFramedVertex(FileModel.class);
            inputPath.setFilePath("src/test/resources/discover-jndi/");

            pm.addFileModel(inputPath);
            pm.setRootFileModel(inputPath);
            WindupConfiguration windupConfiguration = new WindupConfiguration()
                    .setGraphContext(context);
            windupConfiguration.addInputPath(Paths.get(inputPath.getFilePath()));
            windupConfiguration.setOutputDirectory(outputPath);
            windupConfiguration.setOptionValue(SourceModeOption.NAME, true);
            processor.execute(windupConfiguration);

            GraphService<EjbSessionBeanModel> ejbSessionBeanService = new GraphService<>(context, EjbSessionBeanModel.class);
            Iterable<EjbSessionBeanModel> models = ejbSessionBeanService.findAllByProperty(EjbSessionBeanModel.EJB_BEAN_NAME, EJB_NAME);
            Assert.assertEquals(1, Iterables.size(models));
            boolean found = false;
            for (EjbSessionBeanModel model : models) {

                JNDIResourceModel globalJndiReference = model.getGlobalJndiReference();
                if (globalJndiReference != null && JNDI_NAME.equals(globalJndiReference.getJndiLocation()))
                    found = true;
                break;
            }
            assertTrue(found);
        }
    }

}
