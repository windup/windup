package org.jboss.windup.tests;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.addon.engine.WindupProcessor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WindupArchitectureTest
{

   @Deployment
   @Dependencies({
            @AddonDependency(name = "org.jboss.windup.rules:rules"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
   })
   public static ForgeArchive getDeployment()
   {
      ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
               .addBeansXML()
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.windup.rules:rules"),
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
               );
      return archive;
   }

   @Inject
   private WindupProcessor processor;

   @Test
   public void testRunWindup() throws Exception
   {
      Assert.assertNotNull(processor);
      Assert.assertNotNull(processor.toString());

      processor.execute();
   }
}