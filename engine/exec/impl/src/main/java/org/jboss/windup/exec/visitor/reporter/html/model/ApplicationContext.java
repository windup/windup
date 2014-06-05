package org.jboss.windup.exec.visitor.reporter.html.model;

public class ApplicationContext
{
    private final String applicationName;

    public ApplicationContext(String applicationName)
    {
        this.applicationName = applicationName;
    }

    public String getApplicationName()
    {
        return applicationName;
    }
}
