package org.jboss.windup.tests.application;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WindupArchitectureSmallBinaryModeTest extends WindupArchitectureTest
{

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java-ee"),
                @AddonDependency(name = "org.jboss.windup.ext:windup-config-groovy"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClass(WindupArchitectureTest.class)
                    .addAsResource(new File("src/test/groovy/GroovyExampleRule.windup.groovy"))
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java-ee"),
                                AddonDependencyEntry.create("org.jboss.windup.ext:windup-config-groovy"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Test
    public void testRunWindupTiny() throws Exception
    {
        try (GraphContext context = getFactory().create())
        {
            super.runTest(context, "../test-files/jee-example-app-1.0.0.ear", false,
                        Arrays.asList("com.acme"));

            Path graphDirectory = context.getGraphDirectory();
            Path reportsDirectory = graphDirectory.resolve("reports");
            Path indexPath = reportsDirectory.resolve("index.html");

            Path appReportPath = resolveChildPath(reportsDirectory,
                        "JEEExampleApporgwindupexamplejeeexampleapp100\\.\\d\\.html");
            Path appNonClassifiedReportPath = resolveChildPath(reportsDirectory,
                        "nonclassifiedfiles_JEEExampleApporgwindupexamplejeeexampleapp100\\.\\d\\.html");
            Path productCatalogBeanPath = resolveChildPath(reportsDirectory, "ProductCatalogBeanjava\\.\\d\\.html");
            Path loginFilterPath = resolveChildPath(reportsDirectory, "LoginFilterjava\\.\\d\\.html");
            Path loginEventPublisherPath = resolveChildPath(reportsDirectory, "LogEventPublisherjava\\.\\d\\.html");
            Path authenticationFilterPath = resolveChildPath(reportsDirectory, "AuthenticateFilterjava\\.\\d\\.html");
            Path webStartupListenerPath = resolveChildPath(reportsDirectory, "AnvilWebStartupListenerjava\\.\\d\\.html");
            Path webServletPath = resolveChildPath(reportsDirectory, "AnvilWebServletjava\\.\\d\\.html");
            Path webLifecycleListenerPath = resolveChildPath(reportsDirectory,
                        "AnvilWebLifecycleListenerjava\\.\\d\\.html");

            Assert.assertTrue(indexPath.toFile().exists());
            Assert.assertTrue(appReportPath.toFile().exists());
            Assert.assertTrue(appNonClassifiedReportPath.toFile().exists());
            Assert.assertTrue(productCatalogBeanPath.toFile().exists());
            Assert.assertTrue(loginFilterPath.toFile().exists());
            Assert.assertTrue(loginEventPublisherPath.toFile().exists());
            Assert.assertTrue(authenticationFilterPath.toFile().exists());
            Assert.assertTrue(webStartupListenerPath.toFile().exists());
            Assert.assertTrue(webServletPath.toFile().exists());
            Assert.assertTrue(webLifecycleListenerPath.toFile().exists());

            String appReportContent = new String(Files.readAllBytes(appReportPath));
            String webListenerContent = new String(Files.readAllBytes(webLifecycleListenerPath));

            Assert.assertTrue(appReportContent
                        .contains("com.acme.anvil.listener.AnvilWebLifecycleListener"));
            Assert.assertTrue(webListenerContent
                        .contains("This class is proprietary to Weblogic, remove."));
        }

    }

    private Path resolveChildPath(Path parent, final String childPattern)
    {
        String[] list = parent.toFile().list(new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                return name.matches(childPattern);
            }
        });

        if (list == null)
            throw new IllegalStateException("No files matched, for pattern [" + childPattern + "]");

        List<String> results = Arrays.asList(list);
        if (results.size() == 0)
            throw new IllegalStateException("No files matched, for pattern [" + childPattern + "]");

        if (results.size() > 1)
            throw new IllegalStateException("Expected a single result for pattern [" + childPattern + "], but got ["
                        + results.size() + "]: " + results);

        return parent.resolve(results.get(0));
    }

}
