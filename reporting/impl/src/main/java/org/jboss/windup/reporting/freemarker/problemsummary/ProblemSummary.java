package org.jboss.windup.reporting.freemarker.problemsummary;

import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.windup.graph.model.resource.FileModel;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ProblemSummary
{
    private final String ruleID;
    private final String issueName;
    private int numberFound;
    private final int effortPerIncident;
    private final Set<FileModel> files = new LinkedHashSet<>();

    public ProblemSummary(String ruleID, String issueName, int numberFound, int effortPerIncident)
    {
        this.ruleID = ruleID;
        this.issueName = issueName;
        this.numberFound = numberFound;
        this.effortPerIncident = effortPerIncident;
    }

    public String getRuleID()
    {
        return ruleID;
    }

    public String getIssueName()
    {
        return issueName;
    }

    public int getNumberFound()
    {
        return numberFound;
    }

    void setNumberFound(int numberFound)
    {
        this.numberFound = numberFound;
    }

    public int getEffortPerIncident()
    {
        return effortPerIncident;
    }

    public Iterable<FileModel> getFiles()
    {
        return files;
    }

    public void addFile(FileModel fileModel)
    {
        this.files.add(fileModel);
    }
}
