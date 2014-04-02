package org.jboss.windup.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.ui.controller.WizardCommandController;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.WindupService;
import org.jboss.windup.impl.ui.WindupWizard;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

@RunWith(Arquillian.class)
public class WindupWizardTest
{
    @Deployment
    @Dependencies({
             @AddonDependency(name = "org.jboss.windup.legacy.application:legacy-windup"),
             @AddonDependency(name = "org.jboss.forge.addon:ui-test-harness"),
    })
    public static ForgeArchive getDeployment()
    {
       ForgeArchive archive = ShrinkWrap
                .create(ForgeArchive.class)
                .addBeansXML()
                .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"),
                        AddonDependencyEntry.create("org.jboss.forge.addon:ui-test-harness"),
                        AddonDependencyEntry.create("org.jboss.windup.legacy.application:legacy-windup")
                );

       return archive;
    }
    
    @Inject
    private UITestHarness uiTestHarness;
    
    @Inject
    private WindupService windupService;
    
    @Test
    public void testNewMigration() throws Exception {
        Assert.assertNotNull(windupService);
        Assert.assertNotNull(uiTestHarness);
        try (WizardCommandController controller = uiTestHarness.createWizardController(WindupWizard.class)) {
            controller.initialize();
            Assert.assertTrue(controller.isEnabled());
            controller.setValueFor("input", "testinput");
            Assert.assertFalse(controller.canExecute());
            controller.setValueFor("output", "testoutput");
            Assert.assertFalse(controller.canExecute());
            controller.setValueFor("packages", "org.jboss");
            Assert.assertTrue(controller.canExecute());
        }
    }
}
