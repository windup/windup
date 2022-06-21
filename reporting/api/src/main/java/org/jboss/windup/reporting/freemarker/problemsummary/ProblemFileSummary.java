package org.jboss.windup.reporting.freemarker.problemsummary;

import org.jboss.windup.graph.model.resource.FileModel;

/**
 * An immutable class that groups the fileModel with the occurrences for a single hint. This is useful for Issues report.
 */
public class ProblemFileSummary {
    private final FileModel file;

    private int occurrences;

    public ProblemFileSummary(FileModel file, int occurrences) {
        this.file = file;
        this.occurrences = occurrences;
    }

    public void addOccurrence() {
        this.occurrences += 1;
    }

    public FileModel getFile() {
        return file;
    }

    public int getOccurrences() {
        return occurrences;
    }


}
