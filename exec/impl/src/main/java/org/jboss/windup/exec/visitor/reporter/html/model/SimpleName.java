package org.jboss.windup.exec.visitor.reporter.html.model;

public class SimpleName extends Name
{

    public SimpleName(String name)
    {
        super(name);
    }

    @Override
    public String toString()
    {
        return this.getName();
    }
}
