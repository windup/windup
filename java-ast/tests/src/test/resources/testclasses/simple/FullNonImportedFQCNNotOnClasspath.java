package testclasses.simple;

public class FullNonImportedFQCNNotOnClasspath {
    private int foo = com.proprietary.Constants.MY_CONSTANT;
    private int foo2 = OtherConstants.OTHER_CONSTANT;
    private int otherFoo = 2;
}
