package org.jboss.windup.addon.groovy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.loader.WindupRuleProviderLoader;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.RuleBuilder;
import org.ocpsoft.rewrite.context.Context;

@RunWith(Arquillian.class)
/**
 * 
 */
public class LoadGroovyRulesTest
{
    // path to use for the groovy example file in the addon
    private static final String EXAMPLE_GROOVY_FILE = "/org/jboss/windup/addon/groovy/GroovyExampleRule.windup.groovy";

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
                @AddonDependency(name = "org.jboss.windup.ext:windup-config-groovy"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph")
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap
                    .create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsResource(new File("src/test/resources/groovy/GroovyExampleRule.windup.groovy"),
                                EXAMPLE_GROOVY_FILE)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"),
                                AddonDependencyEntry.create("org.jboss.windup.ext:windup-config-groovy"),
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph")
                    );
        return archive;
    }

    @Inject
    private Furnace furnace;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testGroovyRuleProviderFactory() throws Exception
    {
        try (GraphContext context = factory.create())
        {

            Imported<WindupRuleProviderLoader> loaders = furnace.getAddonRegistry().getServices(
                        WindupRuleProviderLoader.class);

            Assert.assertNotNull(loaders);

            List<WindupRuleProvider> allProviders = new ArrayList<WindupRuleProvider>();
            for (WindupRuleProviderLoader loader : loaders)
            {
                allProviders.addAll(loader.getProviders(context));
            }

            boolean foundScriptPath = false;
            for (WindupRuleProvider provider : allProviders)
            {
                Context ruleContext = RuleBuilder.define();
                provider.enhanceMetadata(ruleContext);
                String origin = ((String) ruleContext.get(RuleMetadata.ORIGIN));
                if (origin.contains(EXAMPLE_GROOVY_FILE))
                {
                    foundScriptPath = true;
                    break;
                }
            }
            Assert.assertTrue("Script path should have been set in Rule Metatada", foundScriptPath);
            Assert.assertTrue(allProviders.size() > 0);
            context.getGraph().getBaseGraph().commit();
        }
    }

    @Test
    public void testGroovyUserDirectoryRuleProvider() throws Exception
    {
        try (GraphContext context = factory.create())
        {
            WindupConfigurationModel cfg = WindupConfigurationService.getConfigurationModel(context);

            // create a user path
            Path userRulesPath = Paths.get(FileUtils.getTempDirectory().toString(), "WindupGroovyPath");
            try
            {
                FileUtils.deleteDirectory(userRulesPath.toFile());
                Files.createDirectories(userRulesPath);
                Path exampleGroovyUserDirGroovyFile = userRulesPath.resolve("ExampleUserFile.windup.groovy");

                // copy a groovy rule example to it
                try (InputStream is = getClass().getResourceAsStream(EXAMPLE_GROOVY_FILE);
                            OutputStream os = new FileOutputStream(exampleGroovyUserDirGroovyFile.toFile()))
                {
                    IOUtils.copy(is, os);
                }

                FileService fileModelService = new FileService(context);
                cfg.addUserRulesPath(fileModelService.createByFilePath(userRulesPath.toAbsolutePath().toString()));

                Imported<WindupRuleProviderLoader> loaders = furnace.getAddonRegistry().getServices(
                            WindupRuleProviderLoader.class);

                Assert.assertNotNull(loaders);

                List<WindupRuleProvider> allProviders = new ArrayList<WindupRuleProvider>();
                for (WindupRuleProviderLoader loader : loaders)
                {
                    allProviders.addAll(loader.getProviders(context));
                }

                boolean foundScriptPath = false;
                for (WindupRuleProvider provider : allProviders)
                {
                    Context ruleContext = RuleBuilder.define();
                    provider.enhanceMetadata(ruleContext);
                    String origin = ((String) ruleContext.get(RuleMetadata.ORIGIN));
                    // make sure we found the one from the user dir
                    if (origin.endsWith("ExampleUserFile.windup.groovy"))
                    {
                        foundScriptPath = true;
                        break;
                    }
                }
                Assert.assertTrue("Script path should have been set in Rule Metatada", foundScriptPath);
                Assert.assertTrue(allProviders.size() > 0);
            }
            finally
            {
                FileUtils.deleteDirectory(userRulesPath.toFile());
            }
        }
    }
}
