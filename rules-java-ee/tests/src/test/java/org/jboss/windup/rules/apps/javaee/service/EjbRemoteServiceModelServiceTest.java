package org.jboss.windup.rules.apps.javaee.service;

import java.nio.file.Path;
import java.util.Collections;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.AbstractTest;
import org.jboss.windup.rules.apps.javaee.model.EjbRemoteServiceModel;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Iterables;

/**
 * Tests the {@link EjbRemoteServiceModelService}.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class EjbRemoteServiceModelServiceTest extends AbstractTest {
    @Inject
    private GraphContextFactory factory;

    private Path graphPath;
    private GraphContext context;
    private JavaClassService javaClassService;
    private EjbRemoteServiceModelService serviceModelService;

    @Before
    public void setUp() throws Exception {
        this.graphPath = getDefaultPath();
        this.context = this.factory.create(graphPath, true);
        this.javaClassService = new JavaClassService(this.context);
        this.serviceModelService = new EjbRemoteServiceModelService(this.context);
    }

    @After
    public void tearDown() throws Exception {
        this.context.clear();
        FileUtils.deleteQuietly(this.graphPath.toFile());
    }

    @Test
    public void testGetOrCreate_CreationRequired() throws Exception {
        JavaClassModel interfaceClass = javaClassService.create("com.example.MyServiceInterface");
        JavaClassModel implementationClass = javaClassService.create("com.example.MyServiceInterfaceImplementation");

        Assert.assertFalse(serviceModelService.findAll().iterator().hasNext());

        ProjectModel application = new ProjectService(context).create();
        EjbRemoteServiceModel model = serviceModelService.getOrCreate(Collections.singleton(application), interfaceClass, implementationClass);
        Assert.assertNotNull(model);

        Assert.assertEquals(interfaceClass, model.getInterface());
        Assert.assertEquals(implementationClass, model.getImplementationClass());

        Assert.assertEquals(1, Iterables.size(serviceModelService.findAll()));
    }

    @Test
    public void testGetOrCreate_FindByInterface() throws Exception {
        JavaClassModel interfaceClass = javaClassService.create("com.example.MyServiceInterface");
        JavaClassModel implementationClass = javaClassService.create("com.example.MyServiceInterfaceImplementation");

        EjbRemoteServiceModel testModel = serviceModelService.create();
        testModel.setInterface(interfaceClass);
        testModel.setImplementationClass(implementationClass);

        Assert.assertEquals(1, Iterables.size(serviceModelService.findAll()));

        ProjectModel application = new ProjectService(context).create();
        EjbRemoteServiceModel model = serviceModelService.getOrCreate(Collections.singleton(application), interfaceClass, null);
        Assert.assertNotNull(model);

        Assert.assertEquals(interfaceClass, model.getInterface());
        Assert.assertEquals(implementationClass, model.getImplementationClass());

        Assert.assertEquals(1, Iterables.size(serviceModelService.findAll()));
    }

    @Test
    public void testGetOrCreate_FindByImplementation() throws Exception {
        JavaClassModel interfaceClass = javaClassService.create("com.example.MyServiceInterface");
        JavaClassModel implementationClass = javaClassService.create("com.example.MyServiceInterfaceImplementation");

        EjbRemoteServiceModel testModel = serviceModelService.create();
        testModel.setInterface(interfaceClass);
        testModel.setImplementationClass(implementationClass);

        Assert.assertEquals(1, Iterables.size(serviceModelService.findAll()));

        ProjectModel application = new ProjectService(context).create();
        EjbRemoteServiceModel model = serviceModelService.getOrCreate(Collections.singleton(application), null, implementationClass);
        Assert.assertNotNull(model);

        Assert.assertEquals(interfaceClass, model.getInterface());
        Assert.assertEquals(implementationClass, model.getImplementationClass());

        Assert.assertEquals(1, Iterables.size(serviceModelService.findAll()));
    }

    @Test
    public void testGetOrCreate_FindByInterfaceAndImplementation() throws Exception {
        JavaClassModel interfaceClass = javaClassService.create("com.example.MyServiceInterface");
        JavaClassModel implementationClass = javaClassService.create("com.example.MyServiceInterfaceImplementation");

        EjbRemoteServiceModel testModel = serviceModelService.create();
        testModel.setInterface(interfaceClass);
        testModel.setImplementationClass(implementationClass);

        Assert.assertEquals(1, Iterables.size(serviceModelService.findAll()));

        ProjectModel application = new ProjectService(context).create();
        EjbRemoteServiceModel model = serviceModelService.getOrCreate(Collections.singleton(application), interfaceClass, implementationClass);
        Assert.assertNotNull(model);

        Assert.assertEquals(interfaceClass, model.getInterface());
        Assert.assertEquals(implementationClass, model.getImplementationClass());

        Assert.assertEquals(1, Iterables.size(serviceModelService.findAll()));
    }

    Path getDefaultPath() {
        return FileUtils.getTempDirectory().toPath().resolve("Windup")
                .resolve(getClass().getSimpleName() + "_" + RandomStringUtils.randomAlphanumeric(6));
    }
}