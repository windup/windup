package org.jboss.windup.reporting.freemarker.problemsummary;

import org.jboss.windup.graph.model.resource.FileModel;

/**
 * An immutable class that groups the fileModel with the occurences for a single hint. This is useful for Migration Issues report.
 */
public class ProblemFileSummary
{
    private final FileModel file;

    private int occurences;

    public ProblemFileSummary(FileModel file, int occurences) {
        this.file=file;
        this.occurences=occurences;
    }

    public void addOccurence() {
        this.occurences +=1;
    }

    public FileModel getFile()
    {
        return file;
    }

    public int getOccurences()
    {
        return occurences;
    }



}
