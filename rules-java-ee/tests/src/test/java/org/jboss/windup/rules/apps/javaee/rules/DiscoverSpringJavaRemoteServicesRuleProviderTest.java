package org.jboss.windup.rules.apps.javaee.rules;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.javaee.AbstractTest;
import org.jboss.windup.rules.apps.javaee.model.SpringBeanModel;
import org.jboss.windup.rules.apps.javaee.model.SpringRemoteServiceModel;
import org.jboss.windup.rules.apps.javaee.service.SpringRemoteServiceModelService;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class DiscoverSpringJavaRemoteServicesRuleProviderTest extends AbstractTest
{
    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testFindRemoteServiceOnAnnotatedClass() {
        try (GraphContext context = factory.create(true)) {
            String inputPath = "src/test/resources/spring/remote-services-java";

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

            SpringRemoteServiceModelService springBeanModelGraphService = new SpringRemoteServiceModelService(context);
            List<SpringRemoteServiceModel> allBeans = springBeanModelGraphService.findAll();
            assertEquals(6, allBeans.size());
            assertEquals(1, allBeans.stream().filter(e -> "org.springframework.remoting.rmi.RmiServiceExporter".equalsIgnoreCase(e.getSpringExporterInterface().getQualifiedName())).count());
            assertEquals(1, allBeans.stream().filter(e -> "org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter".equalsIgnoreCase(e.getSpringExporterInterface().getQualifiedName())).count());
            assertEquals(1, allBeans.stream().filter(e -> "org.springframework.remoting.caucho.HessianServiceExporter".equalsIgnoreCase(e.getSpringExporterInterface().getQualifiedName())).count());
            assertEquals(1, allBeans.stream().filter(e -> "org.springframework.remoting.jaxws.SimpleJaxWsServiceExporter".equalsIgnoreCase(e.getSpringExporterInterface().getQualifiedName())).count());
            assertEquals(1, allBeans.stream().filter(e -> "org.springframework.jms.remoting.JmsInvokerServiceExporter".equalsIgnoreCase(e.getSpringExporterInterface().getQualifiedName())).count());
            assertEquals(1, allBeans.stream().filter(e -> "org.springframework.amqp.remoting.service.AmqpInvokerServiceExporter".equalsIgnoreCase(e.getSpringExporterInterface().getQualifiedName())).count());
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert(true);
    }


}
