package org.jboss.windup.tests.windride;


import javax.annotation.security.RunAs;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.loom.MigrationEngine;
import org.jboss.loom.conf.AS7Config;
import org.jboss.loom.conf.Configuration;
import org.jboss.loom.conf.ConfigurationValidator;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WindupArchitectureWindRideTest
{
    /*
     *  I don't know how Forge deps work, so this doesn't use CDI.
     *  It's simply a copy of the test from WindRide.
     */
    
    // Not used.
    /*@Deployment
    @Dependencies({
             @AddonDependency(name = "org.jboss.migr.as:WindRide-engine", version = "1.1.0"),
             @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
       ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                .addBeansXML()
                .addAsAddonDependencies(
                         AddonDependencyEntry.create("org.jboss.migr.as:WindRide-engine"),
                         AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                );
       return archive;
    }*/


    @Test
    @RunAsClient
    public void testRunWindup() throws Exception
    {

         Configuration conf = TestAppConfig.createTestConfig_AS_510_all();
         AS7Config as7Config = conf.getGlobal().getAS7Config();       

         //TestAppConfig.updateAS7ConfAsPerServerMgmtInfo( as7Config );

         TestUtils.announceMigration( conf );
         ConfigurationValidator.validate( conf );
         MigrationEngine migrator = new MigrationEngine( conf );
         migrator.doMigration();

    }
}// class