package org.jboss.windup.tests.application;

import java.io.File;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.service.ArchiveService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaMethodModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WindupArchitectureSmallBinaryMode2Test extends WindupArchitectureTest
{

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
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
                                AddonDependencyEntry.create("org.jboss.windup.ext:windup-config-groovy"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Test
    public void testRunWindupTiny() throws Exception
    {
        try (GraphContext context = createGraphContext())
        {
            super.runTest(context, "../test-files/Windup1x-javaee-example-tiny.war", false);
            validateArchiveHashes(context);
            validateJavaClassModels(context);
        }
    }

    private void validateArchiveHashes(GraphContext context) throws Exception
    {
        ArchiveService archiveService = new ArchiveService(context);
        int numberFound = 0;
        for (ArchiveModel model : archiveService.findAll())
        {
            numberFound++;

            Assert.assertEquals("c60bb0c51623a915cb4a9a90ba9ba70e", model.getMD5Hash());
            Assert.assertEquals("1a1888023eff8629a9e55f023c8ecf63f69fad03", model.getSHA1Hash());
        }
        Assert.assertEquals(1, numberFound);
    }

    private void validateJavaClassModels(GraphContext context)
    {
        JavaClassService service = new JavaClassService(context);

        boolean servletClassFound = false;
        boolean doGetFound = false;
        for (JavaClassModel model : service.findAll())
        {
            if (model.getQualifiedName().equals("org.windup.examples.servlet.SampleServlet"))
            {
                servletClassFound = true;
                int methodsFound = 0;
                for (JavaMethodModel method : model.getJavaMethods())
                {
                    methodsFound++;

                    if (method.getMethodName().equals("doGet"))
                    {
                        doGetFound = true;
                        long paramCount = method.countParameters();
                        Assert.assertEquals(2, paramCount);

                        Assert.assertEquals("javax.servlet.http.HttpServletRequest", method.getParameter(0)
                                    .getJavaType().getQualifiedName());
                        Assert.assertEquals("javax.servlet.http.HttpServletResponse", method.getParameter(1)
                                    .getJavaType().getQualifiedName());
                    }
                }
                Assert.assertEquals(2, methodsFound);
            }
        }
        Assert.assertTrue(servletClassFound);
        Assert.assertTrue(doGetFound);
    }
}
