package org.jboss.windup.rules.apps.yaml;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.yaml.model.YamlFileModel;
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
public class DiscoverYamlFilesRuleProviderTest {
    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Deployment
    @AddonDependencies
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addClass(DiscoverYamlFilesRuleProviderTest.class)
                .addClass(DiscoverYamlFilesRuleProvider.class)
                .addClass(YamlFileModel.class)
                .addBeansXML();
    }

    @Test
    public void testJPAMetadataExtraction() throws Exception {
        try (GraphContext context = factory.create(true)) {
            String inputPath = "src/test/resources/yaml-files/";

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

            GraphService<YamlFileModel> yamlFileModelGraphService = new GraphService<>(context, YamlFileModel.class);
            long yamlFilesFound = yamlFileModelGraphService.findAll().stream().count();
            Assert.assertEquals(1L, yamlFilesFound);
        }
    }
}
