package org.jboss.windup.config.selectables;

import java.nio.file.Path;
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
import org.jboss.windup.config.DefaultEvaluationContext;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.exception.IllegalTypeArgumentException;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

@RunWith(Arquillian.class)
public class VariablesTest
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
    private GraphContextFactory factory;

    @Test
    public void testInvalidTypeGet()
    {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        final GraphContext context = factory.create(folder);
        GraphRewrite event = new GraphRewrite(context);
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);

        JavaClassModel classModel1 = context.getFramed().addVertex(null, JavaClassModel.class);
        classModel1.setQualifiedName("com.example.Class1NoToString");
        JavaClassModel classModel2 = context.getFramed().addVertex(null, JavaClassModel.class);
        classModel2.setQualifiedName("com.example.Class2HasToString");

        Variables vars = Variables.instance(event);
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
                                    + "\"" + MavenProjectModel.class.getName()
                                    + "\", actual implemented interfaces are"));
        }
    }

    @Test
    public void testUnTypedGet()
    {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        final GraphContext context = factory.create(folder);
        GraphRewrite event = new GraphRewrite(context);
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);

        JavaClassModel classModel1 = context.getFramed().addVertex(null, JavaClassModel.class);
        classModel1.setQualifiedName("com.example.Class1NoToString");
        JavaClassModel classModel2 = context.getFramed().addVertex(null, JavaClassModel.class);
        classModel2.setQualifiedName("com.example.Class2HasToString");

        Variables vars = Variables.instance(event);
        vars.push();
        vars.setSingletonVariable("classModel1", classModel1);
        WindupVertexFrame frame = vars.findSingletonVariable("classModel1");
        Assert.assertNotNull(frame);
    }

    @Test
    public void testInvalidCountGet()
    {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        final GraphContext context = factory.create(folder);
        GraphRewrite event = new GraphRewrite(context);
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);

        JavaClassModel classModel1 = context.getFramed().addVertex(null, JavaClassModel.class);
        classModel1.setQualifiedName("com.example.Class1NoToString");
        JavaClassModel classModel2 = context.getFramed().addVertex(null, JavaClassModel.class);
        classModel2.setQualifiedName("com.example.Class2HasToString");

        Variables vars = Variables.instance(event);
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
