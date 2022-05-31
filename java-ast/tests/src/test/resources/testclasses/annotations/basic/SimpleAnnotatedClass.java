package testclasses.annotations.basic;

@SimpleTestAnnotation(value1 = "value 1", value2 = "value 2")
@SimpleSingleMemberAnnotation(SimpleAnnotatedClass.SINGLE_MEMBER_VALUE)
public class SimpleAnnotatedClass {
    public static final String SINGLE_MEMBER_VALUE = "single member value";

}
