package org.jboss.windup.rules.apps.javaee.tests;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.javaee.AbstractTest;
import org.jboss.windup.rules.apps.javaee.model.JPAEntityModel;
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
public class DiscoverJPAAnnotationsTest extends AbstractTest {
    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testJPAMetadataExtraction() throws Exception {
        try (GraphContext context = factory.create(true)) {
            String inputPath = "src/test/resources/jpa";

            Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "windup_"
                    + UUID.randomUUID().toString());
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            WindupConfiguration windupConfiguration = new WindupConfiguration()
                    .setGraphContext(context);
            windupConfiguration.addInputPath(Paths.get(inputPath));
            windupConfiguration.setOutputDirectory(outputPath);
            windupConfiguration.setOptionValue(SourceModeOption.NAME, true);
            processor.execute(windupConfiguration);

            GraphService<JPAEntityModel> jpaEntityModelService = new GraphService<>(context, JPAEntityModel.class);
            int jpaEntitiesFound = 0;
            for (JPAEntityModel jpaEntityModel : jpaEntityModelService.findAll()) {
                //Assert.assertEquals("ChatBeanDestination", msgDriven.getDestination().getJndiLocation());
                if (jpaEntityModel.getEntityName().equals("SubclassWithDiscriminator"))
                    Assert.assertEquals("SimpleEntityTable", jpaEntityModel.getTableName());
                else if (jpaEntityModel.getEntityName().equals("BaseEntity"))
                    Assert.assertEquals("SimpleEntityTable", jpaEntityModel.getTableName());
                else
                    Assert.fail("Unknown entity found with name: " + jpaEntityModel.getEntityName());

                jpaEntitiesFound++;
            }
            Assert.assertEquals(2, jpaEntitiesFound);
        }
    }
}
