package org.jboss.windup.tests.application;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WindupArchitectureSmallBinaryModeTest extends WindupArchitectureTest
{

    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config-groovy"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap.create(AddonArchive.class)
                    .addBeansXML()
                    .addClass(WindupArchitectureTest.class)
                    .addAsResource(new File("src/test/groovy/GroovyExampleRule.windup.groovy"));
    }

    @Test
    public void testRunWindupTiny() throws Exception
    {
        try (GraphContext context = createGraphContext())
        {
            super.runTest(context, "../test-files/jee-example-app-1.0.0.ear", false, Arrays.asList("com.acme"));

            Path graphDirectory = context.getGraphDirectory();
            Path reportsDirectory = graphDirectory.resolve("reports");
            Path indexPath = graphDirectory.resolve(Paths.get("index.html"));

            Path appReportPath = resolveChildPath(reportsDirectory,
                        "Overview_JEE_Example_App__org_windup_example_jee_example_app_1_0_0_\\.html");
            Path appNonClassifiedReportPath = resolveChildPath(reportsDirectory,
                        "nonclassifiedfiles_JEE_Example_App__org_windup_example_jee_example_app_1_0_0_\\.html");
            Path productCatalogBeanPath = resolveChildPath(reportsDirectory, "ProductCatalogBean_java\\.html");

            Assert.assertTrue(indexPath.toFile().exists());
            Assert.assertTrue(appReportPath.toFile().exists());
            Assert.assertTrue(appNonClassifiedReportPath.toFile().exists());
            Assert.assertTrue(productCatalogBeanPath.toFile().exists());

            String appReportContent = new String(Files.readAllBytes(appReportPath));

            Assert.assertTrue(appReportContent
                        .contains("Used only to support migration activities."));
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
