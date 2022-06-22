package simple;

public interface SomeInterface {

    void interfaceMethod();

    default defaultMethod() {
        //java 8 feature test
    }
}
