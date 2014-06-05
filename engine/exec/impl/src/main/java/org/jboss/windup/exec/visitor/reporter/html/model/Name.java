package org.jboss.windup.exec.visitor.reporter.html.model;

public abstract class Name
{
    private final String name;

    public Name(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
