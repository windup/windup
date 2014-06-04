package org.jboss.windup.tests.application;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.addon.engine.WindupProcessor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.tinkerpop.blueprints.Vertex;

@RunWith(Arquillian.class)
public class WindupArchitectureTest
{

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.rules:windup-rules"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.windup.rules:windup-rules"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContext graphContext;

    @Test
    public void testRunWindup() throws Exception
    {
        Assert.assertNotNull(processor);
        Assert.assertNotNull(processor.toString());

        String inputPath = "../../../test_files/Windup1x-javaee-example.war";
        WindupConfigurationModel windupCfg = graphContext.getFramed().addVertex(null, WindupConfigurationModel.class);
        windupCfg.setInputPath(inputPath);
        windupCfg.setSourceMode(false);

        try
        {
            processor.execute();
        }
        finally
        {
            for (Vertex v : graphContext.getGraph().getVertices())
            {
                v.remove();
            }
        }
    }

    @Test
    public void testRunWindupSourceMode() throws Exception
    {
        Assert.assertNotNull(processor);
        Assert.assertNotNull(processor.toString());

        String inputPath = "../../../test_files/src_example";
        WindupConfigurationModel windupCfg = graphContext.getFramed().addVertex(null, WindupConfigurationModel.class);
        windupCfg.setInputPath(inputPath);
        windupCfg.setSourceMode(true);

        try
        {
            processor.execute();
        }
        finally
        {
            for (Vertex v : graphContext.getGraph().getVertices())
            {
                v.remove();
            }
        }
    }
}