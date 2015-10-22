package org.jboss.windup.reporting.freemarker.problemsummary;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
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
    private final Map<FileModel,ProblemFileSummary> files = new LinkedHashMap<>();

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

    public Iterable<ProblemFileSummary> getFiles()
    {
        return files.values();
    }

    public void addFile(FileModel fileModel)
    {
        if(files.containsKey(fileModel)) {
            files.get(fileModel).addOccurence();
        } else {
            files.put(fileModel, new ProblemFileSummary(fileModel,1));
        }
    }
}
