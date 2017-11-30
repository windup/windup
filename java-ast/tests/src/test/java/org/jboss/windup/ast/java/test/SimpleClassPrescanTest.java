package org.jboss.windup.ast.java.test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.windup.ast.java.ClassFileScanner;
import org.jboss.windup.ast.java.data.ClassReference;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class SimpleClassPrescanTest extends AbstractJavaASTTest
{
    private void assertContainsReference(Collection<ClassReference> references, TypeReferenceLocation location, String qualifiedName,
                String packageName, String className, String methodName)
    {
        boolean found = false;
        for (ClassReference reference : references)
        {
            if (reference.getLocation().equals(location) &&
                        StringUtils.equals(reference.getQualifiedName(), qualifiedName) &&
                        StringUtils.equals(reference.getPackageName(), packageName) &&
                        StringUtils.equals(reference.getClassName(), className) &&
                        StringUtils.equals(reference.getMethodName(), methodName))
            {
                found = true;
                break;
            }
        }

        Assert.assertTrue("Could not find reference to: " + qualifiedName, found);
    }

    @Test
    public void testMain()
    {
        Path path = Paths.get("target/test-classes/org/jboss/windup/ast/java/test/testclasses/simple/Main.class");
        ClassFileScanner classFileScanner = new ClassFileScanner(getClassPath());

        Collection<ClassReference> references = classFileScanner.scanClass(path);
        assertContainsReference(references, TypeReferenceLocation.VARIABLE_DECLARATION, "org.jboss.windup.ast.java.test.testclasses.simple.MyBClass",
                    "org.jboss.windup.ast.java.test.testclasses.simple", "MyBClass", null);
        assertContainsReference(references, TypeReferenceLocation.METHOD_CALL,
                    "org.jboss.windup.ast.java.test.testclasses.simple.ClassReturningAnother.returnAnother()",
                    "org.jboss.windup.ast.java.test.testclasses.simple", "ClassReturningAnother", "returnAnother");
        assertContainsReference(references, TypeReferenceLocation.METHOD_CALL,
                    "org.jboss.windup.ast.java.test.testclasses.simple.MyAClass.interfaceMethod()",
                    "org.jboss.windup.ast.java.test.testclasses.simple", "MyAClass", "interfaceMethod");
        assertContainsReference(references, TypeReferenceLocation.METHOD_CALL,
                    "org.jboss.windup.ast.java.test.testclasses.simple.SomeInterface.interfaceMethod()",
                    "org.jboss.windup.ast.java.test.testclasses.simple", "SomeInterface", "interfaceMethod");
    }

    private Set<String> getClassPath()
    {
        Set<String> result = new HashSet<>();
        result.add("target/test-classes/");
        return result;
    }
}
