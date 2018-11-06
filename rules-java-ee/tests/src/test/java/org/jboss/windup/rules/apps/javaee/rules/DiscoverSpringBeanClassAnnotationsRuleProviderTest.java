package org.jboss.windup.rules.apps.javaee.rules;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.javaee.AbstractTest;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RunWith(Arquillian.class)
public class DiscoverSpringBeanClassAnnotationsRuleProviderTest extends AbstractTest
{
    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Inject
    private DiscoverSpringJavaRemoteServicesRuleProvider discoverSpringJavaRemoteServicesRuleProvider;

    @Inject
    private DiscoverSpringBeanMethodAnnotationsRuleProvider discoverSpringBeanMethodAnnotationsRuleProvider;

    @Test
    public void testFindRemoteServiceOnAnnotatedClass() {
        try (GraphContext context = factory.create(true)) {
            String inputPath = "src/test/resources/spring/annotated-bean";

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
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert(true);
    }


}
