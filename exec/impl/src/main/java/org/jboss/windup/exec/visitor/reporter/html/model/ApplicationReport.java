package org.jboss.windup.exec.visitor.reporter.html.model;

import java.util.Set;
import java.util.TreeSet;

public class ApplicationReport
{

    private String applicationName;
    private final Set<ArchiveReport> archives = new TreeSet<ArchiveReport>();

    public void setApplicationName(String applicationName)
    {
        this.applicationName = applicationName;
    }

    public String getApplicationName()
    {
        return applicationName;
    }

    public Set<ArchiveReport> getArchives()
    {
        return archives;
    }
}
