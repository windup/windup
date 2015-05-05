package org.jboss.windup.ast.java.test;

import java.nio.file.Paths;
import java.util.List;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.windup.ast.java.ASTProcessor;
import org.jboss.windup.ast.java.data.ClassReference;
import org.jboss.windup.ast.java.data.annotations.AnnotationArrayValue;
import org.jboss.windup.ast.java.data.annotations.AnnotationClassReference;
import org.jboss.windup.ast.java.data.annotations.AnnotationLiteralValue;
import org.jboss.windup.ast.java.data.annotations.AnnotationValue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JavaAnnotationScanningTest extends AbstractJavaASTTest
{

    @Test
    public void testSimpleAnnotatedClass()
    {
        List<ClassReference> references = ASTProcessor.analyze(getLibraryPaths(), getSourcePaths(),
                    Paths.get("src/test/resources/testclasses/annotations/basic/SimpleAnnotatedClass.java"));

        boolean foundSimpleAnnotation = false;
        boolean foundSingleMemberAnnotation = false;
        for (ClassReference reference : references)
        {
            System.out.println("Reference: " + reference);
            if (reference instanceof AnnotationClassReference)
            {
                AnnotationClassReference annotationReference = (AnnotationClassReference) reference;
                if (reference.getQualifiedName().equals("testclasses.annotations.basic.SimpleTestAnnotation"))
                {
                    Assert.assertEquals("value 1",
                                ((AnnotationLiteralValue) annotationReference.getAnnotationValues().get("value1")).getLiteralValue());
                    Assert.assertEquals("value 2",
                                ((AnnotationLiteralValue) annotationReference.getAnnotationValues().get("value2")).getLiteralValue());
                    foundSimpleAnnotation = true;
                }
                else if (reference.getQualifiedName().equals("testclasses.annotations.basic.SimpleSingleMemberAnnotation"))
                {
                    Assert.assertEquals("single member value",
                                ((AnnotationLiteralValue) annotationReference.getAnnotationValues().get("value")).getLiteralValue());
                    foundSingleMemberAnnotation = true;
                }
            }
        }
        Assert.assertTrue(foundSimpleAnnotation);
        Assert.assertTrue(foundSingleMemberAnnotation);
    }

    @Test
    public void testComplexAnnotatedClass()
    {
        List<ClassReference> references = ASTProcessor.analyze(getLibraryPaths(), getSourcePaths(),
                    Paths.get("src/test/resources/testclasses/annotations/complex/ComplexAnnotatedClass.java"));

        boolean foundAnnotation = false;
        for (ClassReference reference : references)
        {
            System.out.println("Reference: " + reference);
            if (reference instanceof AnnotationClassReference)
            {
                foundAnnotation = true;

                AnnotationClassReference annotation = (AnnotationClassReference) reference;
                Assert.assertEquals("testclasses.annotations.complex.ExampleComplexAnnotation", annotation.getQualifiedName());
                checkLiteralValue(annotation, "booleanValue", boolean.class, true);
                checkLiteralValue(annotation, "byteValue", byte.class, 1);
                checkLiteralValue(annotation, "charValue", char.class, 'a');
                checkLiteralValue(annotation, "doubleValue", double.class, 2.0d);
                checkLiteralValue(annotation, "floatValue", float.class, 3.0f);
                checkLiteralValue(annotation, "intValue", int.class, 4);
                checkLiteralValue(annotation, "key", String.class, "sample string value");
                checkLiteralValue(annotation, "longValue", long.class, 5l);
                checkLiteralValue(annotation, "shortValue", int.class, 6);

                AnnotationClassReference nestedAnnotation = (AnnotationClassReference) annotation.getAnnotationValues().get("nestedAnnotation");
                checkFirstNestedAnnotation(nestedAnnotation);

                AnnotationValue nestedAnnotationArray = annotation.getAnnotationValues().get("nestedAnnotationArray");
                checkNestedAnnotationArray(nestedAnnotationArray);
            }
        }
        Assert.assertTrue(foundAnnotation);
    }

    private void checkFirstNestedAnnotation(AnnotationClassReference nestedAnnotation)
    {
        Assert.assertNotNull(nestedAnnotation);
        Assert.assertEquals("testclasses.annotations.complex.ExampleNestedAnnotation", nestedAnnotation.getQualifiedName());

        AnnotationClassReference nestedLevel2 = (AnnotationClassReference) nestedAnnotation.getAnnotationValues().get("level2");
        Assert.assertNotNull(nestedLevel2);
        Assert.assertEquals("testclasses.annotations.complex.ExampleNestedAnnotationLevel2", nestedLevel2.getQualifiedName());

        AnnotationClassReference firstInnerComplexAnnotation = (AnnotationClassReference) nestedLevel2.getAnnotationValues().get("complex");
        Assert.assertNotNull(firstInnerComplexAnnotation);
        Assert.assertEquals("testclasses.annotations.complex.AnotherComplexAnnotation", firstInnerComplexAnnotation.getQualifiedName());

        AnnotationValue firstInnerArray = firstInnerComplexAnnotation.getAnnotationValues().get("stringArray");
        Assert.assertNotNull(firstInnerArray);
        Assert.assertTrue(firstInnerArray instanceof AnnotationArrayValue);
        AnnotationArrayValue firstInnerArrayValue = (AnnotationArrayValue) firstInnerArray;
        Assert.assertEquals("array value 1", ((AnnotationLiteralValue) firstInnerArrayValue.getValues().get(0)).getLiteralValue());
        Assert.assertEquals(String.class, ((AnnotationLiteralValue) firstInnerArrayValue.getValues().get(0)).getLiteralType());

        Assert.assertEquals("array value 2", ((AnnotationLiteralValue) firstInnerArrayValue.getValues().get(1)).getLiteralValue());
        Assert.assertEquals(String.class, ((AnnotationLiteralValue) firstInnerArrayValue.getValues().get(1)).getLiteralType());
    }

    private void checkNestedAnnotationArray(AnnotationValue nestedAnnotationArray)
    {
        Assert.assertTrue(nestedAnnotationArray instanceof AnnotationArrayValue);
        AnnotationArrayValue arrayValue = (AnnotationArrayValue) nestedAnnotationArray;

        AnnotationClassReference annotation1 = (AnnotationClassReference) arrayValue.getValues().get(0);
        AnnotationClassReference annotation1Level2 = (AnnotationClassReference) annotation1.getAnnotationValues().get("level2");
        AnnotationClassReference annotation1InnerComplex = (AnnotationClassReference) annotation1Level2.getAnnotationValues().get("complex");
        Assert.assertEquals("inside complex annotation (key)",
                    ((AnnotationLiteralValue) annotation1InnerComplex.getAnnotationValues().get("key")).getLiteralValue());
        Assert.assertEquals(String.class, ((AnnotationLiteralValue) annotation1InnerComplex.getAnnotationValues().get("key")).getLiteralType());

        AnnotationArrayValue annotation1InnerArrayValue = (AnnotationArrayValue) annotation1InnerComplex.getAnnotationValues().get("stringArray");
        Assert.assertEquals(2, annotation1InnerArrayValue.getValues().size());
        Assert.assertEquals("array value 1", ((AnnotationLiteralValue) annotation1InnerArrayValue.getValues().get(0)).getLiteralValue());
        Assert.assertEquals(String.class, ((AnnotationLiteralValue) annotation1InnerArrayValue.getValues().get(0)).getLiteralType());
        Assert.assertEquals("from a constant value", ((AnnotationLiteralValue) annotation1InnerArrayValue.getValues().get(1)).getLiteralValue());
        Assert.assertEquals(String.class, ((AnnotationLiteralValue) annotation1InnerArrayValue.getValues().get(1)).getLiteralType());

        AnnotationClassReference annotation2 = (AnnotationClassReference) arrayValue.getValues().get(1);
        AnnotationClassReference annotation2Level2 = (AnnotationClassReference) annotation2.getAnnotationValues().get("level2");
        AnnotationClassReference annotation2InnerComplex = (AnnotationClassReference) annotation2Level2.getAnnotationValues().get("complex");
        Assert.assertEquals("second inside complex annotation (key)",
                    ((AnnotationLiteralValue) annotation2InnerComplex.getAnnotationValues().get("key")).getLiteralValue());
        Assert.assertEquals(String.class, ((AnnotationLiteralValue) annotation2InnerComplex.getAnnotationValues().get("key")).getLiteralType());

        AnnotationArrayValue annotation2InnerArrayValue = (AnnotationArrayValue) annotation2InnerComplex.getAnnotationValues().get("stringArray");
        Assert.assertEquals(3, annotation2InnerArrayValue.getValues().size());
        Assert.assertEquals("second array value 1", ((AnnotationLiteralValue) annotation2InnerArrayValue.getValues().get(0)).getLiteralValue());
        Assert.assertEquals(String.class, ((AnnotationLiteralValue) annotation1InnerArrayValue.getValues().get(0)).getLiteralType());
        Assert.assertEquals("second array value 2", ((AnnotationLiteralValue) annotation2InnerArrayValue.getValues().get(1)).getLiteralValue());
        Assert.assertEquals(String.class, ((AnnotationLiteralValue) annotation1InnerArrayValue.getValues().get(1)).getLiteralType());
        Assert.assertEquals("second value 3", ((AnnotationLiteralValue) annotation2InnerArrayValue.getValues().get(2)).getLiteralValue());
        Assert.assertEquals(String.class, ((AnnotationLiteralValue) annotation2InnerArrayValue.getValues().get(2)).getLiteralType());
    }

    private void checkLiteralValue(AnnotationClassReference annotation, String name, Class<?> expectedType, Object expectedValue)
    {
        AnnotationLiteralValue literalValue = (AnnotationLiteralValue) annotation.getAnnotationValues().get(name);
        Assert.assertEquals(expectedValue, literalValue.getLiteralValue());
        Assert.assertEquals(expectedType, literalValue.getLiteralType());
    }
}
