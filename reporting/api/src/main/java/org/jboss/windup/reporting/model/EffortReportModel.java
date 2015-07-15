package org.jboss.windup.reporting.model;

import com.tinkerpop.frames.Property;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 *  Aggregates the common properties of all the items generating effort for the Application.
 *
 *  @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public interface EffortReportModel extends WindupVertexFrame
{
    static final String TYPE = "ClassificationModel";
    static final String TYPE_PREFIX = TYPE + ":";
    static final String EFFORT = TYPE_PREFIX + "effort";
    static final String SEVERITY = TYPE_PREFIX + TYPE_PREFIX + "severity";

    /**
     * Set the effort weight (E.g. How difficult is it to fix the issue?)
     */
    @Property(EFFORT)
    void setEffort(int effort);

    /**
     * Get the effort weight (E.g. How difficult is it to fix the issue?)
     */
    @Property(EFFORT)
    int getEffort();

    /**
     * Contains a severity level that may be used to indicate to the user the severity level of a problem.
     */
    @Property(SEVERITY)
    void setSeverity(Severity severity);

    /**
     * Contains a severity level that may be used to indicate to the user the severity level of a problem.
     */
    @Property(SEVERITY)
    Severity getSeverity();
}
