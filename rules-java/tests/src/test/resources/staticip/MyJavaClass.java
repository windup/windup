package org.jboss.windup.rules.tests;

public class MyJavaClass {

    String myAddress2 = "10.10.1.1";
    private String myAddress1 = "10.10.1.0";

    public static void main(String[] args) {
        MyJavaClass main = new MyJavaClass();

        System.out.println(main.myAddress1);
        System.out.println(main.myAddress2);

        String myAddress3 = "10.10.1.2";
        System.out.println(myAddress3);

        System.out.println("10.10.1.3");

        // This is a comment: 10.10.1.4
    }
}
