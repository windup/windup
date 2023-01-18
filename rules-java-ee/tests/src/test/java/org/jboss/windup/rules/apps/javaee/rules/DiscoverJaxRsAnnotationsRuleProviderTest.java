package org.jboss.windup.rules.apps.javaee.rules;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.javaee.AbstractTest;
import org.jboss.windup.rules.apps.javaee.model.JaxRSWebServiceModel;
import org.jboss.windup.rules.apps.javaee.service.JaxRSWebServiceModelService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class DiscoverJaxRsAnnotationsRuleProviderTest extends AbstractTest {

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testJaxRsPathAnnotation() throws Exception {
        try (GraphContext context = factory.create(true)) {
            executeAnalysis(context, "src/test/resources/jaxrs");
            List<String> classesFound = new JaxRSWebServiceModelService(context)
                    .findAll()
                    .stream()
                    .map(JaxRSWebServiceModel::getImplementationClass)
                    .map(JavaClassModel::getClassName)
                    .collect(Collectors.toList());
            Assert.assertTrue(classesFound.contains("JakartaUserResource"));
            Assert.assertTrue(classesFound.contains("JavaxUserResource"));
            Assert.assertEquals(2, classesFound.size());
        }
    }

}
