package org.jboss.windup.rules.apps.java.service;

import java.nio.file.Path;
import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaMethodModel;
import org.jboss.windup.rules.apps.java.model.JavaParameterModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JavaMethodServiceTest {

    @AddonDependencies({ @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-base"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi") })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testClassModelGetMethod() throws Exception {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder, true)) {

            JavaClassModel classModel2 = context.getFramed().addFramedVertex(JavaClassModel.class);
            classModel2.setQualifiedName("com.example.Class2HasToString");

            JavaMethodModel methodModelToString = context.getFramed().addFramedVertex(JavaMethodModel.class);
            methodModelToString.setJavaClass(classModel2);
            methodModelToString.setMethodName("toString");

            List<JavaMethodModel> fooMethods = classModel2.getMethod("toString");
            Assert.assertEquals(1, fooMethods.size());
        }
    }

    @Test
    public void testMethodServiceCreate() throws Exception {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder, true)) {

            JavaClassModel classModel1 = context.getFramed().addFramedVertex(JavaClassModel.class);
            classModel1.setQualifiedName("com.example.Class1NoToString");
            JavaClassModel classModel2 = context.getFramed().addFramedVertex(JavaClassModel.class);
            classModel2.setQualifiedName("com.example.Class2HasToString");

            JavaMethodModel methodModelSomeMethod = context.getFramed().addFramedVertex(JavaMethodModel.class);
            methodModelSomeMethod.setJavaClass(classModel1);
            methodModelSomeMethod.setMethodName("foo");
            JavaParameterModel parameter = context.getFramed().addFramedVertex(JavaParameterModel.class);
            parameter.setJavaType(classModel2);
            parameter.setPosition(0);
            methodModelSomeMethod.addMethodParameter(parameter);

            Assert.assertEquals(1, methodModelSomeMethod.countParameters());

            JavaMethodModel methodModelToString = context.getFramed().addFramedVertex(JavaMethodModel.class);
            methodModelToString.setJavaClass(classModel2);
            methodModelToString.setMethodName("toString");

            JavaMethodService methodService = new JavaMethodService(context);
            JavaMethodModel foundMethod = methodService.createJavaMethod(classModel1, "foo", classModel2);
            Assert.assertEquals((Object)methodModelSomeMethod.getId(), (Object)foundMethod.getId());

            methodService.createJavaMethod(classModel1, "bar");
            Assert.assertEquals(2, classModel1.getJavaMethods().size());
        }
    }

}
