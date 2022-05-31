package simple;

public interface SomeInterface {

    default defaultMethod() {
        //java 8 feature test
    }

    void interfaceMethod();
}
