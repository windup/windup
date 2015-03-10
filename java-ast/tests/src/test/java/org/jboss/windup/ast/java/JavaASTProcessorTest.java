package org.jboss.windup.ast.java;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.ast.java.data.JavaClassReference;
import org.jboss.windup.ast.java.data.JavaClassReferences;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JavaASTProcessorTest
{
    private Set<String> libraryPaths = new HashSet<>();
    private Set<String> sourcePaths;

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.ast:windup-java-ast"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.ast:windup-java-ast"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Before
    public void before()
    {
        sourcePaths = new HashSet<>();
        sourcePaths.add("src/test/resources");
    }

    @Test
    public void testHelloWorld()
    {
        JavaClassReferences references = JavaASTProcessor.analyzeJavaFile(libraryPaths, sourcePaths,
                    Paths.get("src/test/resources/testclasses/helloworld/HelloWorld.java"));

        for (JavaClassReference reference : references.getReferences())
        {
            System.out.println("Reference: " + reference);
        }
        Assert.assertTrue(references.getReferences().contains(
                    new JavaClassReference("testclasses.helloworld.HelloWorld", TypeReferenceLocation.TYPE, 3, 0, 174,"public class HelloWorld {")));
        Assert.assertTrue(references.getReferences().contains(
                    new JavaClassReference("testclasses.helloworld.HelloWorld.main(String[])", TypeReferenceLocation.METHOD, 5, 23, 4,"public static void main(String[] argv) throws Exception {")));
        Assert.assertTrue(references.getReferences().contains(
                    new JavaClassReference("void", TypeReferenceLocation.RETURN_TYPE, 5, 4, 108,"public static void main(String[] argv) throws Exception {")));
        Assert.assertTrue(references.getReferences().contains(
                    new JavaClassReference("java.lang.String[]", TypeReferenceLocation.METHOD_PARAMETER, 5, 4, 108,"public static void main(String[] argv) throws Exception {")));
        Assert.assertTrue(references.getReferences().contains(
                    new JavaClassReference("java.lang.Exception", TypeReferenceLocation.THROWS_METHOD_DECLARATION, 5, 51, 9,"public static void main(String[] argv) throws Exception {")));
        Assert.assertTrue(references.getReferences().contains(
                    new JavaClassReference("java.io.PrintStream.println(java.lang.String)", TypeReferenceLocation.METHOD_CALL, 6, 19, 7,"System.out.println(\"Hello world!\")")));

        Assert.assertEquals(6, references.getReferences().size());
    }

    @Test
    public void testSimpleMain()
    {
        JavaClassReferences references = JavaASTProcessor.analyzeJavaFile(libraryPaths, sourcePaths,
                    Paths.get("src/test/resources/testclasses/simple/Main.java"));

        for (JavaClassReference reference : references.getReferences())
        {
            System.out.println("Reference: " + reference);
        }
        Assert.assertTrue(references.getReferences().contains(
                    new JavaClassReference("testclasses.simple.MyBClass", TypeReferenceLocation.VARIABLE_DECLARATION, 10, 7, 28,"MyBClass b=new MyBClass();")));
        Assert.assertTrue(references.getReferences().contains(
                    new JavaClassReference("testclasses.simple.MyAClass.interfaceMethod()", TypeReferenceLocation.METHOD_CALL, 13, 25, 15,"c.returnAnother().interfaceMethod()")));
        Assert.assertTrue(references.getReferences().contains(
                    new JavaClassReference("testclasses.simple.SomeInterface.interfaceMethod()", TypeReferenceLocation.METHOD_CALL, 13, 25, 15,"c.returnAnother().interfaceMethod()")));
        Assert.assertTrue(references.getReferences().contains(
                    new JavaClassReference("testclasses.simple.ClassReturningAnother.returnAnother()", TypeReferenceLocation.METHOD_CALL, 13, 9, 13,"c.returnAnother()")));
    }

    @Test
    public void testMyBClass()
    {
        JavaClassReferences references = JavaASTProcessor.analyzeJavaFile(libraryPaths, sourcePaths,
                    Paths.get("src/test/resources/testclasses/simple/MyBClass.java"));

        for (JavaClassReference reference : references.getReferences())
        {
            System.out.println("Reference: " + reference);
        }

        Assert.assertTrue(references.getReferences().contains(
                    new JavaClassReference("testclasses.simple.MyBClass", TypeReferenceLocation.TYPE, 5, 0, 194,"public class MyBClass extends MyAClass {")));
        Assert.assertTrue(references.getReferences().contains(
                    new JavaClassReference("testclasses.simple.MyAClass", TypeReferenceLocation.TYPE, 5, 0, 194,"public class MyBClass extends MyAClass {")));
    }

    @Test
    public void testMyAClass()
    {
        JavaClassReferences references = JavaASTProcessor.analyzeJavaFile(libraryPaths, sourcePaths,
                    Paths.get("src/test/resources/testclasses/simple/MyAClass.java"));

        for (JavaClassReference reference : references.getReferences())
        {
            System.out.println("Reference: " + reference);
        }
        Assert.assertTrue(references.getReferences().contains(
                    new JavaClassReference("testclasses.simple.MyAClass", TypeReferenceLocation.TYPE, 4, 0, 165,"public class MyAClass implements SomeInterface {")));
        Assert.assertFalse(references.getReferences().contains(
                    new JavaClassReference("testclasses.simple.MyBClass", TypeReferenceLocation.TYPE, 4, 0, 165,"public class MyAClass implements SomeInterface {")));
        Assert.assertTrue(references.getReferences().contains(
                    new JavaClassReference("testclasses.simple.SomeInterface", TypeReferenceLocation.IMPLEMENTS_TYPE, 4, 0, 107,"public class MyAClass implements SomeInterface {")));
    }
}
