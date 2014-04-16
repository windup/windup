package org.jboss.windup.addon.config;

import java.io.File;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationLoader;
import org.ocpsoft.rewrite.config.Subset;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

@RunWith(Arquillian.class)
public class ReadXMLConfigurationTest
{

   @Deployment
   @Dependencies({
            @AddonDependency(name = "org.jboss.windup.addon:config"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
   })
   public static ForgeArchive getDeployment()
   {
      ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
               .addBeansXML()
               .addClass(EvaluationContextImpl.class)
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.windup.addon:config"),
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
               );
      return archive;
   }

   @Test
   public void testRunWindup() throws Exception
   {
      File folder = File.createTempFile("windupGraph", "");
      GraphContext context = new GraphContextImpl(folder);
      ConfigurationLoader loader = ConfigurationLoader.create(context);
      Configuration configuration = loader.loadConfiguration(context);

      EvaluationContextImpl evaluationContext = new EvaluationContextImpl();

      DefaultParameterValueStore values = new DefaultParameterValueStore();
      evaluationContext.put(ParameterValueStore.class, values);

      Subset.evaluate(configuration).perform(new GraphRewrite(), evaluationContext);
   }
}