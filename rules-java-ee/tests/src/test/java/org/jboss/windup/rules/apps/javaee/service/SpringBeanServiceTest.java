package org.jboss.windup.rules.apps.javaee.service;

import javax.inject.Inject;

import com.google.common.collect.Iterables;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.rules.apps.javaee.AbstractTest;
import org.jboss.windup.rules.apps.javaee.model.SpringBeanModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class SpringBeanServiceTest extends AbstractTest {

    @Inject
    GraphContextFactory factory;

    @Test
    public void testSpringFindByProject() throws Exception {
        try (GraphContext context = factory.create(true)) {
            ProjectService projectService = new ProjectService(context);
            ProjectModel app1 = projectService.create();
            app1.setName("app1");

            ProjectModel app2 = projectService.create();
            app2.setName("app2");

            SpringBeanService service = new SpringBeanService(context);
            SpringBeanModel bean1 = service.create();
            bean1.setApplications(Collections.singleton(app1));

            SpringBeanModel bean2 = service.create();
            bean2.setApplications(Collections.singleton(app2));

            service.create();

            Assert.assertEquals(3, Iterables.size(service.findAll()));
            Assert.assertEquals(1, Iterables.size(service.findAllByApplication(app1)));
            Assert.assertEquals(1, Iterables.size(service.findAllByApplication(app2)));
            Assert.assertTrue(Iterables.contains(service.findAllByApplication(app1), bean1));
            Assert.assertTrue(Iterables.contains(service.findAllByApplication(app2), bean2));
        }
    }
}
