package org.jboss.windup.graph;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.tinkerpop.blueprints.Vertex;

@RunWith(Arquillian.class)
public class GremlinGroovyHelperTest
{
    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );

        return archive;
    }

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testGremlinQuery() throws IOException
    {
        Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "Windup",
                    "gremlingroovyhelpertest_" + RandomStringUtils.randomAlphanumeric(6));
        try (GraphContext context = factory.create(outputPath.resolve("graph")))
        {
            GremlinGroovyHelper helper = new GremlinGroovyHelper();
            Vertex v = context.getGraph().addVertex(null);
            v.setProperty("propkey1", "propvalue1");
            v.setProperty("propkey2", "propvalue2");
            v.setProperty("propkey3", "propvalue3");

            String script1 = "#{it.propkey1}==propvalue1 #{it.propkey2}==propvalue2 #{it.propkey3}";
            String script1Result = helper.evaluateEmbeddedScripts(context, v, script1);
            Assert.assertEquals("propvalue1==propvalue1 propvalue2==propvalue2 propvalue3", script1Result);

            String script2 = "This test #{it.propkey1}==propvalue1 #{it.propkey2}==propvalue2 #{it.propkey3}";
            String script2Result = helper.evaluateEmbeddedScripts(context, v, script2);
            Assert.assertEquals("This test propvalue1==propvalue1 propvalue2==propvalue2 propvalue3", script2Result);

            String script3 = "#{it.propkey1}==propvalue1 #{it.propkey2}==propvalue2 #{it.propkey3} more text";
            String script3Result = helper.evaluateEmbeddedScripts(context, v, script3);
            Assert.assertEquals("propvalue1==propvalue1 propvalue2==propvalue2 propvalue3 more text", script3Result);

        }
    }
}
