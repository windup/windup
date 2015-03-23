package org.jboss.windup.ast.java.test;

import java.nio.file.Paths;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.windup.ast.java.ASTProcessor;
import org.jboss.windup.ast.java.data.ClassReference;
import org.jboss.windup.ast.java.data.ClassReferences;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JavaASTProcessorTest extends AbstractJavaASTTest
{

    @Test
    public void testHelloWorld()
    {
        ClassReferences references = ASTProcessor.analyzeJavaFile(getLibraryPaths(), getSourcePaths(),
                Paths.get("src/test/resources/testclasses/helloworld/HelloWorld.java"));

        for (ClassReference reference : references.getReferences())
        {
            System.out.println("Reference: " + reference);
        }
        Assert.assertTrue(references.getReferences().contains(
                    new ClassReference("testclasses.helloworld.HelloWorld", TypeReferenceLocation.TYPE, 3, 0, 174, "public class HelloWorld {")));
        Assert.assertTrue(references.getReferences().contains(
                    new ClassReference("testclasses.helloworld.HelloWorld.main(String[])", TypeReferenceLocation.METHOD, 5, 23, 4,
                                "public static void main(String[] argv) throws Exception {")));
        Assert.assertTrue(references.getReferences().contains(
                    new ClassReference("void", TypeReferenceLocation.RETURN_TYPE, 5, 4, 108,
                                "public static void main(String[] argv) throws Exception {")));
        Assert.assertTrue(references.getReferences().contains(
                    new ClassReference("java.lang.String[]", TypeReferenceLocation.METHOD_PARAMETER, 5, 4, 108,
                                "public static void main(String[] argv) throws Exception {")));
        Assert.assertTrue(references.getReferences().contains(
                    new ClassReference("java.lang.Exception", TypeReferenceLocation.THROWS_METHOD_DECLARATION, 5, 51, 9,
                                "public static void main(String[] argv) throws Exception {")));
        Assert.assertTrue(references.getReferences().contains(
                    new ClassReference("java.io.PrintStream.println(java.lang.String)", TypeReferenceLocation.METHOD_CALL, 6, 19, 7,
                                "System.out.println(\"Hello world!\")")));

        Assert.assertEquals(6, references.getReferences().size());
    }

    @Test
    public void testSimpleMain()
    {
        ClassReferences references = ASTProcessor.analyzeJavaFile(getLibraryPaths(), getSourcePaths(),
                Paths.get("src/test/resources/testclasses/simple/Main.java"));

        for (ClassReference reference : references.getReferences())
        {
            System.out.println("Reference: " + reference);
        }
        Assert.assertTrue(references.getReferences().contains(
                    new ClassReference("testclasses.simple.MyBClass", TypeReferenceLocation.VARIABLE_DECLARATION, 9, 8, 28,
                                "MyBClass b=new MyBClass();")));
        Assert.assertTrue(references.getReferences().contains(
                    new ClassReference("testclasses.simple.MyAClass.interfaceMethod()", TypeReferenceLocation.METHOD_CALL, 12, 26, 15,
                                "c.returnAnother().interfaceMethod()")));
        Assert.assertTrue(references.getReferences().contains(
                    new ClassReference("testclasses.simple.SomeInterface.interfaceMethod()", TypeReferenceLocation.METHOD_CALL, 12, 26, 15,
                                "c.returnAnother().interfaceMethod()")));
        Assert.assertTrue(references.getReferences().contains(
                    new ClassReference("testclasses.simple.ClassReturningAnother.returnAnother()", TypeReferenceLocation.METHOD_CALL, 12, 10, 13,
                                "c.returnAnother()")));
    }

    @Test
    public void testMyBClass()
    {
        ClassReferences references = ASTProcessor.analyzeJavaFile(getLibraryPaths(), getSourcePaths(),
                Paths.get("src/test/resources/testclasses/simple/MyBClass.java"));

        for (ClassReference reference : references.getReferences())
        {
            System.out.println("Reference: " + reference);
        }

        Assert.assertTrue(references.getReferences().contains(
                    new ClassReference("testclasses.simple.MyBClass", TypeReferenceLocation.TYPE, 4, 0, 161,
                                "public class MyBClass extends MyAClass {")));
        Assert.assertTrue(references.getReferences().contains(
                    new ClassReference("testclasses.simple.MyAClass", TypeReferenceLocation.TYPE, 4, 0, 161,
                                "public class MyBClass extends MyAClass {")));
    }

    @Test
    public void testMyAClass()
    {
        ClassReferences references = ASTProcessor.analyzeJavaFile(getLibraryPaths(), getSourcePaths(),
                Paths.get("src/test/resources/testclasses/simple/MyAClass.java"));

        for (ClassReference reference : references.getReferences())
        {
            System.out.println("Reference: " + reference);
        }
        Assert.assertTrue(references.getReferences().contains(
                    new ClassReference("testclasses.simple.MyAClass", TypeReferenceLocation.TYPE, 3, 0, 128,
                                "public class MyAClass implements SomeInterface {")));
        Assert.assertTrue(references.getReferences().contains(
                    new ClassReference("testclasses.simple.SomeInterface", TypeReferenceLocation.IMPLEMENTS_TYPE, 3, 0, 99,
                                "public class MyAClass implements SomeInterface {")));
    }
    @Test
    public void testJavaLangReferences()
    {
        ClassReferences references = ASTProcessor.analyzeJavaFile(getLibraryPaths(), getSourcePaths(),
                Paths.get("src/test/resources/testclasses/javalang/JavaLangReferences.java"));

        for (ClassReference reference : references.getReferences())
        {
            System.out.println("Reference: " + reference);
        }
        Assert.assertTrue(references.getReferences().contains(
                    new ClassReference("testclasses.javalang.JavaLangReferences", TypeReferenceLocation.TYPE, 3, 0, 191,
                                "public class JavaLangReferences {")));
        Assert.assertTrue(references.getReferences().contains(
                    new ClassReference("void", TypeReferenceLocation.RETURN_TYPE, 5, 4, 119, "public void someMethod(){")));
        Assert.assertTrue(references.getReferences().contains(
                    new ClassReference("java.lang.String", TypeReferenceLocation.VARIABLE_DECLARATION, 7, 8, 39,
                                "String a=\"This is an example String\";")));
        Assert.assertTrue(references.getReferences().contains(
                    new ClassReference("java.lang.String", TypeReferenceLocation.VARIABLE_DECLARATION, 8, 8, 26, "String b=a.substring(1);")));
        Assert.assertTrue(references.getReferences().contains(
                    new ClassReference("java.lang.String.substring(int)", TypeReferenceLocation.METHOD_CALL, 8, 21, 9, "a.substring(1)")));

    }
}
