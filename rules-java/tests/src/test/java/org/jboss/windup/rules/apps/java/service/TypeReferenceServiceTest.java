package org.jboss.windup.rules.apps.java.service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.ast.java.data.ResolutionStatus;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TypeReferenceServiceTest
{

    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-base"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment()
    {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testGetPackageUseFrequencies() throws Exception
    {
        try (GraphContext context = factory.create())
        {
            Assert.assertNotNull(context);

            TypeReferenceService typeReferenceService = new TypeReferenceService(context);

            ProjectModel projectModel = fillData(context);

            Set<String> emptySet = Collections.emptySet();
            Map<String, Integer> data = typeReferenceService.getPackageUseFrequencies(projectModel, emptySet, emptySet, 2, false);
            Assert.assertEquals(1, data.size());
            Assert.assertEquals("com.example.*", data.keySet().iterator().next());
            Assert.assertEquals(Integer.valueOf(2), data.values().iterator().next());
        }
    }

    private ProjectModel fillData(GraphContext context)
    {
        InlineHintService inlineHintService = new InlineHintService(context);
        TypeReferenceService typeReferenceService = new TypeReferenceService(context);
        FileModel f1 = context.getFramed().addVertex(null, FileModel.class);
        f1.setFilePath("/f1");
        FileModel f2 = context.getFramed().addVertex(null, FileModel.class);
        f2.setFilePath("/f2");

        JavaTypeReferenceModel t1 = typeReferenceService.createTypeReference(f1, TypeReferenceLocation.ANNOTATION, ResolutionStatus.RESOLVED, 0, 2,
                    2,
                    "com.example.Class1", "@Class1");
        JavaTypeReferenceModel t2 = typeReferenceService.createTypeReference(f1, TypeReferenceLocation.ANNOTATION, ResolutionStatus.RESOLVED, 0, 2,
                    2,
                    "com.example.Class1", "@Class1");

        InlineHintModel b1 = inlineHintService.create();
        InlineHintModel b1b = inlineHintService.create();
        b1.setFile(f1);
        b1.setFileLocationReference(t1);
        b1b.setFile(f1);
        b1b.setFileLocationReference(t2);

        ProjectModel projectModel = context.getFramed().addVertex(null, ProjectModel.class);
        projectModel.addFileModel(f1);
        f1.setProjectModel(projectModel);
        projectModel.addFileModel(f2);
        f2.setProjectModel(projectModel);

        return projectModel;
    }
}
