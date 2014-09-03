package org.jboss.windup.tests.application;

import java.io.File;
import java.util.Iterator;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.engine.WindupProcessor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.Service;
import org.jboss.windup.rules.apps.javaee.model.EjbDeploymentDescriptorModel;
import org.jboss.windup.rules.apps.javaee.model.EjbMessageDrivenModel;
import org.jboss.windup.rules.apps.javaee.model.EjbSessionBeanModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WindupArchitectureJEEExampleTest extends WindupArchitectureTest
{
    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java-ee"),
                @AddonDependency(name = "org.jboss.windup.utils:utils"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.ext:windup-config-groovy"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClass(WindupArchitectureTest.class)
                    .addAsResource(new File("src/test/groovy/GroovyExampleRule.windup.groovy"))
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java-ee"),
                                AddonDependencyEntry.create("org.jboss.windup.utils:utils"),
                                AddonDependencyEntry.create("org.jboss.windup.reporting:windup-reporting"),
                                AddonDependencyEntry.create("org.jboss.windup.ext:windup-config-groovy"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContext graphContext;

    @Test
    public void testRunWindupJEEExampleMode() throws Exception
    {
        // The test-files folder in the project root dir.
        super.runTest(processor, graphContext, "../../test-files/jee-example-app-1.0.0.ear", false);

        validateEjbXmlReferences();
    }

    /**
     * Validate that a ejb-jar.xml file was found, and that the metadata was extracted correctly
     */
    private void validateEjbXmlReferences()
    {
        Service<EjbDeploymentDescriptorModel> ejbXmlService = new GraphService<>(graphContext,
                    EjbDeploymentDescriptorModel.class);
        Iterator<EjbDeploymentDescriptorModel> models = ejbXmlService.findAll().iterator();

        // There should be at least one file
        Assert.assertTrue(models.hasNext());
        EjbDeploymentDescriptorModel model = models.next();

        // and only one file
        Assert.assertFalse(models.hasNext());

        int sessionBeansFound = 0;
        for (EjbSessionBeanModel sessionBean : model.getEjbSessionBeans())
        {
            if (sessionBean.getBeanName().equals("ItemLookupBean"))
            {
                Assert.assertEquals("com.acme.anvil.service.ItemLookupHome", sessionBean.getEjbHome()
                            .getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ItemLookup", sessionBean.getEjbRemote().getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ItemLookupLocalHome", sessionBean.getEjbLocalHome()
                            .getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ItemLookupLocal", sessionBean.getEjbLocal()
                            .getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ItemLookupBean", sessionBean.getEjbClass()
                            .getQualifiedName());
                Assert.assertEquals("Stateless", sessionBean.getSessionType());
                Assert.assertEquals("Container", sessionBean.getTransactionType());
            }
            else if (sessionBean.getBeanName().equals("ProductCatalogBean"))
            {
                Assert.assertEquals("com.acme.anvil.service.ProductCatalogHome", sessionBean.getEjbHome()
                            .getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ProductCatalog", sessionBean.getEjbRemote()
                            .getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ProductCatalogLocalHome", sessionBean.getEjbLocalHome()
                            .getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ProductCatalogLocal", sessionBean.getEjbLocal()
                            .getQualifiedName());
                Assert.assertEquals("com.acme.anvil.service.ProductCatalogBean", sessionBean.getEjbClass()
                            .getQualifiedName());
                Assert.assertEquals("Stateless", sessionBean.getSessionType());
                Assert.assertEquals("Container", sessionBean.getTransactionType());
            }
            else
            {
                Assert.fail("Unrecognized session bean found: " + sessionBean.getBeanName());
            }
            sessionBeansFound++;
        }
        Assert.assertEquals(2, sessionBeansFound);

        int messageDrivenFound = 0;
        for (EjbMessageDrivenModel messageDriven : model.getMessageDriven())
        {
            Assert.assertEquals("LogEventSubscriber", messageDriven.getBeanName());
            Assert.assertEquals("com.acme.anvil.service.jms.LogEventSubscriber", messageDriven.getEjbClass()
                        .getQualifiedName());
            Assert.assertEquals("Container", messageDriven.getTransactionType());
            messageDrivenFound++;
        }
        Assert.assertEquals(1, messageDrivenFound);
    }
}
