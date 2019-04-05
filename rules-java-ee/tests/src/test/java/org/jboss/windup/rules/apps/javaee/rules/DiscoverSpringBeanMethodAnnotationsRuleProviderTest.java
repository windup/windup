package org.jboss.windup.rules.apps.javaee.rules;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.javaee.AbstractTest;
import org.jboss.windup.rules.apps.javaee.model.EjbMessageDrivenModel;
import org.jboss.windup.rules.apps.javaee.model.SpringBeanModel;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class DiscoverSpringBeanMethodAnnotationsRuleProviderTest extends AbstractTest
{
    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;


    @Test
    public void testFindRemoteServiceOnAnnotatedClass() {
        try (GraphContext context = factory.create(true)) {
            String inputPath = "src/test/resources/spring/annotated-method-bean";

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

            GraphService<SpringBeanModel> springBeanModelGraphService = new GraphService<>(context, SpringBeanModel.class);
            List<SpringBeanModel> allBeans = springBeanModelGraphService.findAll();
            assertEquals(2, allBeans.size());
            assertTrue(allBeans.stream().anyMatch(e -> "com.whatever.windup.MyImplementation".equalsIgnoreCase(e.getJavaClass().getQualifiedName()) &&
                                                  e.getJavaClass().getInterfaces().stream()
                                                          .anyMatch(intf -> intf.getQualifiedName().equalsIgnoreCase("com.whatever.windup.MyInterface"))));
            assertTrue(allBeans.stream().anyMatch(e -> "com.whatever.windup.MyOtherImplementation".equalsIgnoreCase(e.getJavaClass().getQualifiedName()) &&
                                                  e.getJavaClass().getInterfaces().stream()
                                                          .anyMatch(intf -> intf.getQualifiedName().equalsIgnoreCase("com.whatever.windup.MyOtherInterface"))));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}