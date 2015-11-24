package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.IndexType;
import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 *  Aggregates the common properties of all the items generating effort for the Application.
 *
 *  @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
@TypeValue(EffortReportModel.TYPE)
public interface EffortReportModel extends WindupVertexFrame
{
    String TYPE = "EffortReportModel";
    String TYPE_PREFIX = TYPE + ":";
    String EFFORT = "EffortReportModelEffort"; // don't use the prefix as we can't name the index with an "_"
    String SEVERITY = TYPE_PREFIX + "severity";

    /**
     * Set the effort weight (E.g. How difficult is it to fix the issue?)
     */
    @Property(EFFORT)
    void setEffort(int effort);

    /**
     * Get the effort weight (E.g. How difficult is it to fix the issue?)
     */
    @Property(EFFORT)
    @Indexed(value = IndexType.SEARCH, dataType = Integer.class)
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
