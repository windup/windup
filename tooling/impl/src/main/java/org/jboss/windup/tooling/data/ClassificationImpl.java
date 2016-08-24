package org.jboss.windup.tooling.data;

import java.io.File;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.Severity;
import org.jboss.windup.tooling.data.Classification;
import org.jboss.windup.tooling.data.Link;

/**
 * This is a non-graph dependent analogue to {@link ClassificationModel} suitable for usage after the {@link GraphContext} itself has been closed.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ClassificationImpl implements Classification
{
    private final Object id;
    private File file;
    private String classification;
    private String description;
    private Iterable<Link> links;
    private int effort;
    private Severity severity;
    private String ruleID;

    /**
     * Constructs a {@link Classification} with the given id.
     */
    public ClassificationImpl(Object id)
    {
        this.id = id;
    }

    /**
     * Contains the unique identifier for the {@link Classification}.
     */
    public Object getID()
    {
        return id;
    }

    /**
     * This is the {@link File} that this {@link Classification} refers to.
     */
    @Override
    public File getFile()
    {
        return file;
    }

    /**
     * This is the {@link File} that this {@link Classification} refers to.
     */
    public void setFile(File file)
    {
        this.file = file;
    }

    /**
     * Contains a description of this {@link Classification}. This is similar in concept to a title, and should be one sentence or less.
     */
    @Override
    public String getClassification()
    {
        return classification;
    }

    /**
     * Contains a description of this {@link Classification}. This is similar in concept to a title, and should be one sentence or less.
     */
    public void setClassification(String classification)
    {
        this.classification = classification;
    }

    /**
     * Contains a description of the file and any migration steps that may be necessary.
     */
    @Override
    public String getDescription()
    {
        return description;
    }

    /**
     * Contains a description of the file and any migration steps that may be necessary.
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * This contains a list of {@link Link}s for further information about the problem and its solution.
     */
    @Override
    public Iterable<Link> getLinks()
    {
        return links;
    }

    /**
     * This contains a list of {@link Link}s for further information about the problem and its solution.
     */
    public void setLinks(Iterable<Link> links)
    {
        this.links = links;
    }

    /**
     * This contains the effort level as an integer (Story Points). This is based on the Scrum "modified-Fibonacci" system of effort estimation.
     */
    @Override
    public int getEffort()
    {
        return effort;
    }

    /**
     * This contains the effort level as an integer (Story Points). This is based on the Scrum "modified-Fibonacci" system of effort estimation.
     */
    public void setEffort(int effort)
    {
        this.effort = effort;
    }

    /**
     * This is a hint as to the severity of the problem. This may be used for supplying an icon or glyph in the report to the user.
     */
    @Override
    public Severity getSeverity()
    {
        return severity;
    }

    /**
     * This is a hint as to the severity of the problem. This may be used for supplying an icon or glyph in the report to the user.
     */
    public void setSeverity(Severity severity)
    {
        this.severity = severity;
    }

    /**
     * This contains the id of the rule that produced this {@link Classification}.
     */
    @Override
    public String getRuleID()
    {
        return ruleID;
    }

    /**
     * This contains the id of the rule that produced this {@link Classification}.
     */
    public void setRuleID(String ruleID)
    {
        this.ruleID = ruleID;
    }
}
