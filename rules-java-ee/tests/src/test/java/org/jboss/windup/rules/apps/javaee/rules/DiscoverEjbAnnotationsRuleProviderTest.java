package org.jboss.windup.rules.apps.javaee.rules;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.javaee.AbstractTest;
import org.jboss.windup.rules.apps.javaee.model.EjbBeanBaseModel;
import org.jboss.windup.rules.apps.javaee.model.EjbMessageDrivenModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(Arquillian.class)
public class DiscoverEjbAnnotationsRuleProviderTest extends AbstractTest {

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testEJBMessageDrivenAnnotation() throws Exception {
        try (GraphContext context = factory.create(true)) {
            executeAnalysis(context, "src/test/resources/ejb/mdb/annotation");
            List<String> classesFound = new GraphService<>(context, EjbMessageDrivenModel.class)
                    .findAll()
                    .stream()
                    .map(EjbBeanBaseModel::getEjbClass)
                    .map(JavaClassModel::getClassName)
                    .collect(Collectors.toList());
            Assert.assertTrue(classesFound.contains("JavaxEJBMessageDrivenAnnotated"));
            Assert.assertTrue(classesFound.contains("JakartaEJBMessageDrivenAnnotated"));
            Assert.assertEquals(2, classesFound.size());
        }
    }

}
