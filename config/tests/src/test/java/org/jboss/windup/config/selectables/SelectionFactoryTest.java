package org.jboss.windup.config.selectables;

import java.io.File;

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
import org.jboss.windup.graph.GraphApiCompositeClassLoaderProvider;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextImpl;
import org.jboss.windup.graph.model.meta.xml.MavenFacetModel;
import org.jboss.windup.graph.typedgraph.GraphTypeRegistry;
import org.jboss.windup.rules.apps.java.scan.model.JavaClassModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Arquillian.class)
public class SelectionFactoryTest
{

    private static final Logger LOG = LoggerFactory.getLogger(SelectionFactoryTest.class);

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

    @Inject
    private SelectionFactory selectionFactory;

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
        event.getRewriteContext().put(SelectionFactory.class, selectionFactory);

        JavaClassModel classModel1 = context.getFramed().addVertex(null, JavaClassModel.class);
        classModel1.setQualifiedName("com.example.Class1NoToString");
        JavaClassModel classModel2 = context.getFramed().addVertex(null, JavaClassModel.class);
        classModel2.setQualifiedName("com.example.Class2HasToString");

        selectionFactory.push();
        selectionFactory.setCurrentPayload("classModel1", classModel1);
        try
        {
            selectionFactory.getCurrentPayload(MavenFacetModel.class, "classModel1");
        }
        catch (IllegalTypeArgumentException e)
        {
            Assert.assertNotNull(e.getMessage());
            Assert.assertTrue(
                        e.getMessage()
                                    .contains("Variable \"classModel1\" does not implement expected interface \"org.jboss.windup.graph.model.meta.xml.MavenFacetModel\", actual implemented interfaces are"));
        }
    }
}
