package org.jboss.windup.reporting.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.IndexType;
import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.reporting.category.IssueCategory;
import org.jboss.windup.reporting.category.IssueCategoryModel;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;

import java.util.Iterator;

/**
 * Aggregates the common properties of all the items generating effort for the Application.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
@TypeValue(EffortReportModel.TYPE)
public interface EffortReportModel extends WindupVertexFrame {
    String TYPE = "EffortReportModel";
    String TYPE_PREFIX = TYPE + "-";
    String ISSUE_CATEGORY = TYPE_PREFIX + "issueCategory";
    String EFFORT = "EffortReportModelEffort"; // don't use the prefix as we can't name the index with special characters

    /**
     * Get the effort weight (E.g. How difficult is it to fix the issue?)
     */
    @Property(EFFORT)
    @Indexed(value = IndexType.SEARCH, dataType = Integer.class)
    int getEffort();

    /**
     * Set the effort weight (E.g. How difficult is it to fix the issue?)
     */
    @Property(EFFORT)
    void setEffort(int effort);

    /**
     * Contains a the id of the {@link IssueCategory} (for example, mandatory or potential).
     */
    default IssueCategoryModel getIssueCategory() {
        Iterator<Vertex> categoryVertices = getElement().vertices(Direction.OUT, ISSUE_CATEGORY);

        IssueCategoryModel result;
        if (categoryVertices.hasNext()) {
            result = getGraph().frameElement(categoryVertices.next(), IssueCategoryModel.class);
        } else {
            result = IssueCategoryRegistry.loadFromGraph(getGraph(), IssueCategoryRegistry.DEFAULT);
        }
        return result;
    }

    /**
     * Contains a the id of the {@link IssueCategory} (for example, mandatory or potential).
     */
    @Adjacency(label = ISSUE_CATEGORY, direction = Direction.OUT)
    void setIssueCategory(IssueCategoryModel issueCategory);

}
