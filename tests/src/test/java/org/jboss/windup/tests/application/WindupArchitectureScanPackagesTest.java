package org.jboss.windup.tests.application;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WindupArchitectureScanPackagesTest extends WindupArchitectureTest
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
    public void testRunWindupMedium() throws Exception
    {
        final String path = "../test-files/Windup1x-javaee-example.war";
        List<String> includePackages = Collections.singletonList("org.apache.wicket.ajax");
        List<String> excludePackages = Collections.emptyList();

        try (GraphContext context = getFactory().create())
        {
            super.runTest(context, path, false, includePackages, excludePackages);

            GraphService<FileModel> fileModelService = new GraphService<>(context, FileModel.class);
            boolean foundHintedFile = false;
            boolean foundAjaxHintedFile = false;
            boolean foundNonAjaxHintedFile = false;

            InlineHintService inlineHintService = new InlineHintService(context);

            for (FileModel fileModel : fileModelService.findAll())
            {
                String pkg = null;
                if (fileModel instanceof JavaClassFileModel)
                {
                    pkg = ((JavaClassFileModel) fileModel).getPackageName();
                }
                else if (fileModel instanceof JavaSourceFileModel)
                {
                    pkg = ((JavaSourceFileModel) fileModel).getPackageName();
                }

                if (pkg == null)
                {
                    continue;
                }
                Iterable<InlineHintModel> hintIterable = inlineHintService.getHintsForFile(fileModel);
                if (hintIterable.iterator().hasNext())
                {
                    foundHintedFile = true;
                    if (pkg.startsWith("org.apache.wicket.ajax"))
                    {
                        foundAjaxHintedFile = true;
                    }
                    else
                    {
                        foundNonAjaxHintedFile = true;
                    }
                }
            }

            Assert.assertTrue(foundHintedFile);
            Assert.assertTrue(foundAjaxHintedFile);
            Assert.assertFalse(foundNonAjaxHintedFile);
        }
    }

}
