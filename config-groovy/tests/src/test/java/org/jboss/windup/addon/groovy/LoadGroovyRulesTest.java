package org.jboss.windup.addon.groovy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.loader.RuleProviderLoader;
import org.jboss.windup.config.metadata.RuleMetadataType;
import org.jboss.windup.graph.GraphContextFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.config.RuleBuilder;
import org.ocpsoft.rewrite.context.Context;

@RunWith(Arquillian.class)
/**
 *
 */
public class LoadGroovyRulesTest {
    // path to use for the groovy example file in the addon
    private static final String EXAMPLE_GROOVY_WINDUP_FILE = "/org/jboss/windup/addon/groovy/GroovyExampleRule.windup.groovy";
    private static final String EXAMPLE_GROOVY_RHAMT_FILE = "/org/jboss/windup/addon/groovy/GroovyExampleRule.rhamt.groovy";
    private static final String EXAMPLE_GROOVY_MTA_FILE = "/org/jboss/windup/addon/groovy/GroovyExampleRule.mta.groovy";

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        AddonArchive archive = ShrinkWrap
                .create(AddonArchive.class)
                .addBeansXML()
                .addAsResource(new File("src/test/resources/groovy/GroovyExampleRule.windup.groovy"), EXAMPLE_GROOVY_WINDUP_FILE)
                .addAsResource(new File("src/test/resources/groovy/GroovyExampleRule.rhamt.groovy"), EXAMPLE_GROOVY_RHAMT_FILE)
                .addAsResource(new File("src/test/resources/groovy/GroovyExampleRule.mta.groovy"), EXAMPLE_GROOVY_MTA_FILE);
        return archive;
    }

    @Inject
    private Furnace furnace;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testGroovyRuleProviderFactory() throws Exception {
        RuleLoaderContext ruleLoaderContext = new RuleLoaderContext();
        Imported<RuleProviderLoader> loaders = furnace.getAddonRegistry().getServices(
                RuleProviderLoader.class);

        Assert.assertNotNull(loaders);

        List<RuleProvider> allProviders = new ArrayList<>();
        for (RuleProviderLoader loader : loaders) {
            allProviders.addAll(loader.getProviders(ruleLoaderContext));
        }

        boolean foundRuleProviderOrigin = false;
        boolean foundRuleOrigin = false;
        boolean foundRhamtRuleProviderOrigin = false;
        boolean foundRhamtRuleOrigin = false;
        boolean foundMtaRuleProviderOrigin = false;
        boolean foundMtaRuleOrigin = false;
        for (RuleProvider provider : allProviders) {
            String providerOrigin = provider.getMetadata().getOrigin();
            if (providerOrigin.contains(EXAMPLE_GROOVY_WINDUP_FILE)) {
                foundRuleProviderOrigin = true;
            }

            if (providerOrigin.contains(EXAMPLE_GROOVY_RHAMT_FILE)) {
                foundRhamtRuleProviderOrigin = true;
            }

            if (providerOrigin.contains(EXAMPLE_GROOVY_MTA_FILE)) {
                foundMtaRuleProviderOrigin = true;
            }

            Rule rule = RuleBuilder.define();
            Context ruleContext = (Context) rule;

            AbstractRuleProvider.enhanceRuleMetadata(provider, rule);

            String ruleOrigin = ((String) ruleContext.get(RuleMetadataType.ORIGIN));
            if (ruleOrigin.contains(EXAMPLE_GROOVY_WINDUP_FILE)) {
                foundRuleOrigin = true;
            } else if (ruleOrigin.contains(EXAMPLE_GROOVY_RHAMT_FILE)) {
                foundRhamtRuleOrigin = true;
            } else if (ruleOrigin.contains(EXAMPLE_GROOVY_MTA_FILE)) {
                foundMtaRuleOrigin = true;
            }
        }
        Assert.assertTrue("Script path should have been set in Rule Metatada", foundRuleOrigin);
        Assert.assertTrue("Script path should have been set in Rule Provider Metatada", foundRuleProviderOrigin);
        Assert.assertTrue("Script path should have been set in RHAMT Rule Metatada", foundRhamtRuleOrigin);
        Assert.assertTrue("Script path should have been set in RHAMT Rule Provider Metatada", foundRhamtRuleProviderOrigin);
        Assert.assertTrue("Script path should have been set in MTA Rule Metatada", foundMtaRuleOrigin);
        Assert.assertTrue("Script path should have been set in MTA Rule Provider Metatada", foundMtaRuleProviderOrigin);
        Assert.assertTrue(allProviders.size() > 0);
    }

    @Test
    public void testGroovyUserDirectoryRuleProvider() throws Exception {
        // create a user path
        Path userRulesPath = Paths.get(FileUtils.getTempDirectory().toString(), "WindupGroovyPath");
        try {
            FileUtils.deleteDirectory(userRulesPath.toFile());
            Files.createDirectories(userRulesPath);
            Path exampleGroovyUserDirWindupGroovyFile = userRulesPath.resolve("ExampleUserFile.windup.groovy");
            Path exampleGroovyUserDirRhamtGroovyFile = userRulesPath.resolve("ExampleUserFile.rhamt.groovy");
            Path exampleGroovyUserDirMtaGroovyFile = userRulesPath.resolve("ExampleUserFile.mta.groovy");

            // copy a groovy rule example to it
            try (InputStream is = getClass().getResourceAsStream(EXAMPLE_GROOVY_WINDUP_FILE);
                 OutputStream os = new FileOutputStream(exampleGroovyUserDirWindupGroovyFile.toFile())) {
                IOUtils.copy(is, os);
            }

            try (InputStream is = getClass().getResourceAsStream(EXAMPLE_GROOVY_RHAMT_FILE);
                 OutputStream os = new FileOutputStream(exampleGroovyUserDirRhamtGroovyFile.toFile())) {
                IOUtils.copy(is, os);
            }

            try (InputStream is = getClass().getResourceAsStream(EXAMPLE_GROOVY_MTA_FILE);
                 OutputStream os = new FileOutputStream(exampleGroovyUserDirMtaGroovyFile.toFile())) {
                IOUtils.copy(is, os);
            }

            RuleLoaderContext ruleLoaderContext = new RuleLoaderContext(Collections.singleton(userRulesPath), null);

            Imported<RuleProviderLoader> loaders = furnace.getAddonRegistry().getServices(
                    RuleProviderLoader.class);

            Assert.assertNotNull(loaders);

            List<RuleProvider> allProviders = new ArrayList<>();
            for (RuleProviderLoader loader : loaders) {
                allProviders.addAll(loader.getProviders(ruleLoaderContext));
            }

            boolean foundScriptPath = false;
            boolean foundRhamtScriptPath = false;
            boolean foundMtaScriptPath = false;
            for (RuleProvider provider : allProviders) {
                Rule rule = RuleBuilder.define();
                Context ruleContext = (Context) rule;

                AbstractRuleProvider.enhanceRuleMetadata(provider, rule);

                String origin = ((String) ruleContext.get(RuleMetadataType.ORIGIN));

                if (origin.endsWith("ExampleUserFile.windup.groovy")) {
                    // make sure we found the one from the user dir
                    foundScriptPath = true;
                } else if (origin.endsWith("ExampleUserFile.rhamt.groovy")) {
                    // make sure we found the one from the user dir
                    foundRhamtScriptPath = true;
                } else if (origin.endsWith("ExampleUserFile.mta.groovy")) {
                    // make sure we found the one from the user dir
                    foundMtaScriptPath = true;
                }
            }
            Assert.assertTrue("Script path should have been set in Rule Metatada", foundScriptPath);
            Assert.assertTrue("Script path should have been set in RHAMT Rule Metatada", foundRhamtScriptPath);
            Assert.assertTrue("Script path should have been set in MTA Rule Metatada", foundMtaScriptPath);
            Assert.assertTrue(allProviders.size() > 0);
        } finally {
            FileUtils.deleteDirectory(userRulesPath.toFile());
        }
    }
}
