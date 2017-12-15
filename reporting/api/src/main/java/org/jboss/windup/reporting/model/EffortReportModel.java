package org.jboss.windup.reporting.model;

import com.thinkaurelius.titan.core.TitanGraph;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.blueprints.util.wrappers.event.EventGraph;
import com.syncleus.ferma.annotations.Adjacency;
import com.syncleus.ferma.FramedGraph;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import org.jboss.windup.graph.IndexType;
import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.syncleus.ferma.annotations.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.reporting.category.IssueCategory;
import org.jboss.windup.reporting.category.IssueCategoryModel;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;

/**
 * Aggregates the common properties of all the items generating effort for the Application.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
@TypeValue(EffortReportModel.TYPE)
public interface EffortReportModel extends WindupVertexFrame
{
    String TYPE = "EffortReportModel";
    String TYPE_PREFIX = TYPE + "-";
    String EFFORT = "EffortReportModelEffort"; // don't use the prefix as we can't name the index with special characters
    String ISSUE_CATEGORY = TYPE_PREFIX + "issueCategory";

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
     * Contains a the id of the {@link IssueCategory} (for example, mandatory or potential).
     */
    @Adjacency(label = ISSUE_CATEGORY, direction = Direction.OUT)
    void setIssueCategory(IssueCategoryModel issueCategory);

    /**
     * Contains a the id of the {@link IssueCategory} (for example, mandatory or potential).
     */
    @JavaHandler
    IssueCategoryModel getIssueCategory();

    abstract class Impl implements EffortReportModel, JavaHandlerContext<Vertex>
    {
        @Override
        public IssueCategoryModel getIssueCategory()
        {
            Iterable<Vertex> categoryVertices = it().getVertices(Direction.OUT, ISSUE_CATEGORY);

            IssueCategoryModel result;
            if (categoryVertices.iterator().hasNext())
            {
                result = frame(categoryVertices.iterator().next());
            }
            else
            {
                result = IssueCategoryRegistry.loadFromGraph((FramedGraph<EventGraph<TitanGraph>>)g(), IssueCategoryRegistry.DEFAULT);
            }
            return result;
        }
    }
}
