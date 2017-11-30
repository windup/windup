package org.jboss.windup.ast.java.test.testclasses.simple;

public class ClassReturningAnother
{
    public MyAClass returnAnother() {
        return new MyAClass();
    }
}
