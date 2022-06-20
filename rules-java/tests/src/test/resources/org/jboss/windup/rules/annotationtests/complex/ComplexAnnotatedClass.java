package org.jboss.windup.rules.annotationtests.complex;

@ExampleComplexAnnotation(
        booleanValue = true,
        byteValue = (byte) 1,
        charValue = 'a',
        doubleValue = 2.0d,
        floatValue = 3.0f,
        intValue = 4,
        key = "sample string value",
        longValue = 5l,
        nestedAnnotation =
        @ExampleNestedAnnotation(
                level2 =
                @ExampleNestedAnnotationLevel2
                        (
                                complex = @AnotherComplexAnnotation
                                        (
                                                stringArray = {"array value 1", "array value 2"},
                                                key = "inside complex annotation (key)"
                                        )
                        )
        ),
        nestedAnnotationArray = {
                @ExampleNestedAnnotation(
                        level2 =
                        @ExampleNestedAnnotationLevel2
                                (
                                        complex = @AnotherComplexAnnotation
                                                (
                                                        stringArray = {"array value 1", ComplexAnnotatedClass.CONSTANT_VALUE},
                                                        key = "inside complex annotation (key)"
                                                )
                                )
                ),
                @ExampleNestedAnnotation(
                        level2 =
                        @ExampleNestedAnnotationLevel2
                                (
                                        complex = @AnotherComplexAnnotation
                                                (
                                                        stringArray = {"second array value 1", "second array value 2", "second value 3"},
                                                        key = "second inside complex annotation (key)"
                                                )
                                )
                )
        },
        shortValue = 6,
        stringArray = {"String 1", "String 2"})
public class ComplexAnnotatedClass {
    public static final String CONSTANT_VALUE = "from a constant value";
}
