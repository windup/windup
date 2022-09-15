package simple;

public class Main {
    public static void main(String[] argv) throws Exception {
        //should be instantiation of MyAClass also (MIGR-228)
        MyBClass b = new MyBClass();
        ClassReturningAnother c = new ClassReturningAnother();
        //should be called on interface, should be called on the MyAClass
        c.returnAnother().interfaceMethod();
    }
}
