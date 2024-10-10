package org.jboss.windup.rules.apps.javaee.service;

import java.nio.file.Path;

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
import org.jboss.windup.rules.apps.javaee.model.JaxRSWebServiceModel;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Iterables;

/**
 * Tests the {@link JaxRSWebServiceModelService}.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class JaxRSWebServiceModelServiceTest extends AbstractTest {

    @Inject
    private GraphContextFactory factory;

    private Path graphPath;
    private GraphContext context;
    private JavaClassService javaClassService;
    private JaxRSWebServiceModelService serviceModelService;

    @Before
    public void setUp() throws Exception {
        this.graphPath = getDefaultPath();
        this.context = this.factory.create(graphPath, true);
        this.javaClassService = new JavaClassService(this.context);
        this.serviceModelService = new JaxRSWebServiceModelService(this.context);
    }

    @After
    public void tearDown() throws Exception {
        this.context.clear();
        FileUtils.deleteQuietly(this.graphPath.toFile());
    }

    @Test
    public void testGetOrCreate_CreationRequired() throws Exception {
        String path = "/path/to/com.example.MyService";
        JavaClassModel implementationClass = javaClassService.create("com.example.MyServiceInterfaceImplementation");

        Assert.assertFalse(serviceModelService.findAll().iterator().hasNext());

        ProjectModel application = new ProjectService(context).create();
        JaxRSWebServiceModel model = serviceModelService.getOrCreate(application, path, implementationClass);
        Assert.assertNotNull(model);

        Assert.assertEquals(path, model.getPath());
        Assert.assertEquals(implementationClass, model.getImplementationClass());

        Assert.assertEquals(1, Iterables.size(serviceModelService.findAll()));
    }

    @Test
    public void testGetOrCreate_FindByPath() throws Exception {
        String path = "/path/to/com.example.MyService";
        JavaClassModel implementationClass = javaClassService.create("com.example.MyServiceInterfaceImplementation");

        JaxRSWebServiceModel testModel = serviceModelService.create();
        testModel.setPath(path);
        testModel.setImplementationClass(implementationClass);

        Assert.assertEquals(1, Iterables.size(serviceModelService.findAll()));

        ProjectModel application = new ProjectService(context).create();
        JaxRSWebServiceModel model = serviceModelService.getOrCreate(application, path, null);
        Assert.assertNotNull(model);

        Assert.assertEquals(path, model.getPath());
        Assert.assertEquals(implementationClass, model.getImplementationClass());

        Assert.assertEquals(1, Iterables.size(serviceModelService.findAll()));
    }

    @Test
    public void testGetOrCreate_FindByImplementation() throws Exception {
        String path = "/path/to/com.example.MyService";
        JavaClassModel implementationClass = javaClassService.create("com.example.MyServiceInterfaceImplementation");

        JaxRSWebServiceModel testModel = serviceModelService.create();
        testModel.setPath(path);
        testModel.setImplementationClass(implementationClass);

        Assert.assertEquals(1, Iterables.size(serviceModelService.findAll()));

        ProjectModel application = new ProjectService(context).create();
        JaxRSWebServiceModel model = serviceModelService.getOrCreate(application, path, null);
        Assert.assertNotNull(model);

        Assert.assertEquals(path, model.getPath());
        Assert.assertEquals(implementationClass, model.getImplementationClass());

        Assert.assertEquals(1, Iterables.size(serviceModelService.findAll()));
    }

    @Test
    public void testGetOrCreate_FindByPathAndImplementation() throws Exception {
        String path = "/path/to/com.example.MyService";
        JavaClassModel implementationClass = javaClassService.create("com.example.MyServiceInterfaceImplementation");

        JaxRSWebServiceModel testModel = serviceModelService.create();
        testModel.setPath(path);
        testModel.setImplementationClass(implementationClass);

        Assert.assertEquals(1, Iterables.size(serviceModelService.findAll()));

        ProjectModel application = new ProjectService(context).create();
        JaxRSWebServiceModel model = serviceModelService.getOrCreate(application, path, null);
        Assert.assertNotNull(model);

        Assert.assertEquals(path, model.getPath());
        Assert.assertEquals(implementationClass, model.getImplementationClass());

        Assert.assertEquals(1, Iterables.size(serviceModelService.findAll()));
    }

    Path getDefaultPath() {
        return FileUtils.getTempDirectory().toPath().resolve("Windup")
                .resolve(getClass().getSimpleName() + "_" + RandomStringUtils.randomAlphanumeric(6));
    }
}