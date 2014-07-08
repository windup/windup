package org.jboss.windup.config.selectables;

import java.io.File;
import java.util.Arrays;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.exception.IllegalTypeArgumentException;
import org.jboss.windup.config.runner.DefaultEvaluationContext;
import org.jboss.windup.config.runner.VarStack;
import org.jboss.windup.graph.GraphApiCompositeClassLoaderProvider;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextImpl;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.typedgraph.GraphTypeRegistry;
import org.jboss.windup.rules.apps.java.scan.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.scan.model.project.MavenProjectModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

@RunWith(Arquillian.class)
public class VarStackTest
{
    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private GraphApiCompositeClassLoaderProvider graphApiCompositeClassLoaderProvider;

    @Inject
    private GraphTypeRegistry graphTypeRegistry;

    @Test
    public void testInvalidTypeGet()
    {
        final File folder = OperatingSystemUtils.createTempDir();
        final GraphContext context = new GraphContextImpl(folder, graphTypeRegistry,
                    graphApiCompositeClassLoaderProvider);
        GraphRewrite event = new GraphRewrite(context);
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);

        JavaClassModel classModel1 = context.getFramed().addVertex(null, JavaClassModel.class);
        classModel1.setQualifiedName("com.example.Class1NoToString");
        JavaClassModel classModel2 = context.getFramed().addVertex(null, JavaClassModel.class);
        classModel2.setQualifiedName("com.example.Class2HasToString");

        VarStack vars = VarStack.instance(event);
        vars.push();
        vars.setSingletonVariable("classModel1", classModel1);
        try
        {
            vars.findSingletonVariable(MavenProjectModel.class, "classModel1");
        }
        catch (IllegalTypeArgumentException e)
        {
            Assert.assertNotNull(e.getMessage());
            Assert.assertTrue(e
                        .getMessage()
                        .contains("Variable \"classModel1\" does not implement expected interface "
                                    + "\"org.jboss.windup.rules.apps.maven.model.MavenProjectModel\", actual implemented interfaces are"));
        }
    }

    @Test
    public void testInvalidCountGet()
    {
        final File folder = OperatingSystemUtils.createTempDir();
        final GraphContext context = new GraphContextImpl(folder, graphTypeRegistry,
                    graphApiCompositeClassLoaderProvider);
        GraphRewrite event = new GraphRewrite(context);
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);

        JavaClassModel classModel1 = context.getFramed().addVertex(null, JavaClassModel.class);
        classModel1.setQualifiedName("com.example.Class1NoToString");
        JavaClassModel classModel2 = context.getFramed().addVertex(null, JavaClassModel.class);
        classModel2.setQualifiedName("com.example.Class2HasToString");

        VarStack vars = VarStack.instance(event);
        vars.push();
        vars.setVariable("classModel1", Arrays.asList((WindupVertexFrame) classModel1, classModel2));
        try
        {
            vars.findSingletonVariable(MavenProjectModel.class, "classModel1");
        }
        catch (IllegalStateException e)
        {
            Assert.assertNotNull(e.getMessage());
            Assert.assertTrue(e.getMessage().contains("More than one frame present"));
        }
    }
}
