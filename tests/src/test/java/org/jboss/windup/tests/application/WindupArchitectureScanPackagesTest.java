package org.jboss.windup.tests.application;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
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
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
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
    public void testRunWindupScanPackages() throws Exception
    {
        final String path = "../test-files/Windup1x-javaee-example.war";
        List<String> includePackages = Collections.singletonList("org.apache.wicket.application");
        List<String> excludePackages = Collections.emptyList();

        try (GraphContext context = createGraphContext())
        {
            super.runTest(context, path, null, false, includePackages, excludePackages);

            validateInlineHintsInAppropriatePackages(context);
        }
    }

    private void validateInlineHintsInAppropriatePackages(GraphContext context)
    {
        GraphService<FileModel> fileModelService = new GraphService<>(context, FileModel.class);
        boolean foundHintedFile = false;
        boolean foundAppHintedFile = false;
        boolean foundNonAppHintedFile = false;

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
            for (InlineHintModel hint : hintIterable)
            {
                foundHintedFile = true;
                if (pkg.startsWith("org.apache.wicket.application"))
                {
                    foundAppHintedFile = true;
                }
                else if (hint.getDescription().equals("127.0.0.1") && fileModel.getFileName().equals("MockHttpServletRequest.java"))
                {
                    // these are ok (results of a ip address scan that is file based, rather than package based
                }
                else
                {
                    System.out.println("Unexpected hinted file found: " + fileModel.getFullPath() + " hint: " + hint.getTitle() + " desc: "
                                + hint.getDescription());
                    foundNonAppHintedFile = true;
                }
            }
        }

        Assert.assertTrue(foundHintedFile);
        Assert.assertTrue(foundAppHintedFile);
        Assert.assertFalse(foundNonAppHintedFile);
    }

}
