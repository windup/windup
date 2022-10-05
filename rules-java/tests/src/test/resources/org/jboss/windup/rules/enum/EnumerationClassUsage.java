package testclasses.enumeration;

import java.nio.file.AccessMode;

public class EnumerationClassUsage {
    public EnumerationClassUsage(AccessMode testmode) {
        this.testMode = testmode;
    }

    AccessMode testMode = AccessMode.WRITE;

    public static void main(String[] args) {
        System.out.println(AccessMode.READ + " test");
        EnumerationClassUsage.testAccessibleEnum(AccessMode.WRITE);
    }

    private static void testAccessibleEnum(AccessMode mode) {
        //something
    }
}

class TestEnum {

    public static void main(String[] args) {
        EnumerationClassUsage test = new EnumerationClassUsage(AccessMode.READ);
    }

}

