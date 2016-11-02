package org.jboss.windup.reporting.severity;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * Provides a way to represent {@link IssueCategory}s in the graph.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(IssueCategoryModel.TYPE)
public interface IssueCategoryModel extends WindupVertexFrame
{
    String TYPE = "IssueCategoryModel";
    String CATEGORY_ID = "categoryID";
    String ORIGIN = "origin";
    String NAME = "name";
    String DESCRIPTION = "description";
    String PRIORITY = "priority";

    @Property(CATEGORY_ID)
    String getCategoryID();

    @Property(CATEGORY_ID)
    void setCategoryID(String categoryID);

    @Property(ORIGIN)
    String getOrigin();

    @Property(ORIGIN)
    void setOrigin(String origin);

    @Property(NAME)
    String getName();

    @Property(NAME)
    void setName(String name);

    @Property(DESCRIPTION)
    String getDescription();

    @Property(DESCRIPTION)
    void setDescription(String description);

    @Property(PRIORITY)
    Integer getPriority();

    @Property(PRIORITY)
    void setPriority(Integer priority);
}
