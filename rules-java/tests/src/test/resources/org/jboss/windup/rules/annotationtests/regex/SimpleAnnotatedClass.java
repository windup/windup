package org.jboss.windup.rules.annotationtests.regex;

@SimpleTestAnnotation(value1 = "value 1", value2 = "value 2")
@SimpleTestAnnotation(value1 = "value 3", value2 = "value 4")
@SimpleSingleMemberAnnotation(SimpleAnnotatedClass.SINGLE_MEMBER_VALUE)
public class SimpleAnnotatedClass
{
    public static final String SINGLE_MEMBER_VALUE = "single member value";

    @SimpleTestAnnotation(value1 = "member value 1", value2 = "member value 2")
    private String testString = "test";

}
