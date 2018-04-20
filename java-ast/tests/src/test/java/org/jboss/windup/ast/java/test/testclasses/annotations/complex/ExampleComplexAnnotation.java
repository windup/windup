package org.jboss.windup.ast.java.test.testclasses.annotations.complex;

public @interface ExampleComplexAnnotation
{
    String key();

    org.jboss.windup.ast.java.test.testclasses.annotations.complex.ExampleNestedAnnotation nestedAnnotation();

    ExampleNestedAnnotation[] nestedAnnotationArray();

    String[] stringArray();

    boolean booleanValue();

    byte byteValue();

    char charValue();

    double doubleValue();

    float floatValue();

    int intValue();

    short shortValue();

    long longValue();
}
