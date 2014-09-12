package org.jboss.windup.rules.apps.java.service;

import java.util.Map;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaInlineHintModel;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JavaInlineHintServiceTest
{

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private GraphContext context;

    @Test
    public void testGetPackageUseFrequencies() throws Exception
    {
        Assert.assertNotNull(context);
        context.init(null);

        JavaInlineHintService javaInlineHintService = new JavaInlineHintService(context);

        ProjectModel projectModel = fillData();

        Map<String, Integer> data = javaInlineHintService.getPackageUseFrequencies(projectModel, 2, false);
        Assert.assertEquals(1, data.size());
        Assert.assertEquals("com.example.*", data.keySet().iterator().next());
        Assert.assertEquals(Integer.valueOf(2), data.values().iterator().next());
    }

    private ProjectModel fillData()
    {
        TypeReferenceService typeReferenceService = new TypeReferenceService(context);
        JavaInlineHintService javaInlineHintService = new JavaInlineHintService(context);

        FileModel f1 = context.getFramed().addVertex(null, FileModel.class);
        f1.setFilePath("/f1");
        FileModel f2 = context.getFramed().addVertex(null, FileModel.class);
        f2.setFilePath("/f2");

        TypeReferenceModel t1 = typeReferenceService.createTypeReference(f1, TypeReferenceLocation.ANNOTATION, 0, 2, 2,
                    "com.example.Class1");
        TypeReferenceModel t2 = typeReferenceService.createTypeReference(f1, TypeReferenceLocation.ANNOTATION, 0, 2, 2,
                    "com.example.Class1");

        JavaInlineHintModel b1 = javaInlineHintService.create();
        JavaInlineHintModel b1b = javaInlineHintService.create();
        b1.setFile(f1);
        b1.setTypeReferenceModel(t1);
        b1b.setFile(f1);
        b1b.setTypeReferenceModel(t2);

        ProjectModel projectModel = context.getFramed().addVertex(null, ProjectModel.class);
        projectModel.addFileModel(f1);
        f1.setProjectModel(projectModel);
        projectModel.addFileModel(f2);
        f2.setProjectModel(projectModel);

        return projectModel;
    }
}
