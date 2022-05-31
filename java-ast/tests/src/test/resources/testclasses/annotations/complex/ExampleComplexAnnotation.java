package testclasses.annotations.complex;

public @interface ExampleComplexAnnotation {
    String key();

    ExampleNestedAnnotation nestedAnnotation();

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
