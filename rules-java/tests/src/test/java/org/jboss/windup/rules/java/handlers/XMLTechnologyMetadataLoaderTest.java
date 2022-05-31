package org.jboss.windup.rules.java.handlers;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.metadata.TechnologyMetadata;
import org.jboss.windup.config.metadata.TechnologyMetadataProvider;
import org.jboss.windup.config.metadata.TechnologyReference;
import org.jboss.windup.config.parser.XMLRuleProviderLoader;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class XMLTechnologyMetadataLoaderTest {

    public static final String TEST1_TECHNOLOGY_METADATA_XML = "Test1.technology.metadata.xml";
    @Inject
    private XMLRuleProviderLoader loader;
    @Inject
    private GraphContextFactory graphContextFactory;
    @Inject
    private TechnologyMetadataProvider provider;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-xml"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addAsResource(new File("src/test/resources/Test1.technology.metadata.xml"), TEST1_TECHNOLOGY_METADATA_XML);
    }

    @Test
    public void testMetadataLoad() throws Exception {
        try (GraphContext context = graphContextFactory.create(true)) {
            WindupConfigurationModel cfg = WindupConfigurationService.getConfigurationModel(context);

            Path rulesPath = FileUtils.getTempDirectory().toPath().resolve("Windup")
                    .resolve("testrulespath_" + RandomStringUtils.randomAlphanumeric(6));
            Files.createDirectories(rulesPath);
            try (InputStream is = getClass().getResourceAsStream("/" + TEST1_TECHNOLOGY_METADATA_XML);
                 OutputStream os = new FileOutputStream(rulesPath.resolve("Test1.technology.metadata.xml").toFile())) {
                IOUtils.copy(is, os);
            }

            cfg.addUserRulesPath(new FileService(context).createByFilePath(rulesPath.toString()));

            TechnologyMetadata metadata = provider.getMetadata(context, new TechnologyReference("eap", "[6.0]"));
            Assert.assertNotNull(metadata);

            Assert.assertNull(provider.getMetadata(context, new TechnologyReference("notpresent", "[6.0]")));
        }
    }
}
