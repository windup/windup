package org.jboss.windup.rules.tests;

public class Main {

    private String myAddress1 = "10.10.1.0";
    String myAddress2 = "10.10.1.1";

    public static void main(String[] args) {
        Main main = new Main();

        System.out.println(main.myAddress1);
        System.out.println(main.myAddress2);

        String myAddress3 = "10.10.1.2";
        System.out.println(myAddress3);

        System.out.println("10.10.1.3");

        // This is a comment: 10.10.1.4
    }
}
