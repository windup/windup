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
import org.jboss.windup.rules.apps.javaee.model.RMIServiceModel;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.Iterables;

/**
 * Tests the {@link RMIServiceModelService}.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class RMIServiceModelServiceTest extends AbstractTest {
    @Inject
    private GraphContextFactory factory;

    private Path graphPath;
    private GraphContext context;
    private JavaClassService javaClassService;
    private RMIServiceModelService rmiService;

    @Before
    public void setUp() throws Exception {
        this.graphPath = getDefaultPath();
        this.context = this.factory.create(graphPath, true);
        this.javaClassService = new JavaClassService(this.context);
        this.rmiService = new RMIServiceModelService(this.context);
    }

    @After
    public void tearDown() throws Exception {
        this.context.clear();
        FileUtils.deleteQuietly(this.graphPath.toFile());
    }

    @Test
    public void testGetOrCreate_CreationRequired() throws Exception {
        // create a class representing an rmi interface
        JavaClassModel rmiClass = javaClassService.create("com.example.MyRMIService");

        Assert.assertFalse(rmiService.findAll().iterator().hasNext());

        ProjectModel application = new ProjectService(context).create();
        RMIServiceModel model = rmiService.getOrCreate(application, rmiClass);
        Assert.assertNotNull(model);

        Iterable<RMIServiceModel> allModels = rmiService.findAll();
        Assert.assertEquals(1, Iterables.size(allModels));
    }

    @Test
    public void testGetOrCreate_FindExisting() throws Exception {
        // create a class representing an rmi interface
        JavaClassModel rmiClass = javaClassService.create("com.example.MyRMIService");

        RMIServiceModel manuallyCreated = rmiService.create();
        manuallyCreated.setInterface(rmiClass);

        Assert.assertEquals(1, Iterables.size(rmiService.findAll()));

        ProjectModel application = new ProjectService(context).create();
        RMIServiceModel model = rmiService.getOrCreate(application, rmiClass);
        Assert.assertNotNull(model);

        Assert.assertEquals(1, Iterables.size(rmiService.findAll()));
    }

    Path getDefaultPath() {
        return FileUtils.getTempDirectory().toPath().resolve("Windup")
                .resolve(getClass().getSimpleName() + "_" + RandomStringUtils.randomAlphanumeric(6));
    }
}