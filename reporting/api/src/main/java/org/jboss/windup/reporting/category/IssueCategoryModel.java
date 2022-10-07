package org.jboss.windup.reporting.category;

import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import java.util.Comparator;

/**
 * Provides a way to represent {@link IssueCategory}s in the graph.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(IssueCategoryModel.TYPE)
public interface IssueCategoryModel extends WindupVertexFrame {
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

    class IssueSummaryPriorityComparator implements Comparator<IssueCategoryModel> {
        @Override
        public int compare(IssueCategoryModel issueCategory1, IssueCategoryModel issueCategory2) {
            int ordinal1 = issueCategory1 == null ? 0 : issueCategory1.getPriority();
            String id1 = issueCategory1 == null ? "" : issueCategory1.getCategoryID();
            int ordinal2 = issueCategory2 == null ? 0 : issueCategory2.getPriority();
            String id2 = issueCategory2 == null ? "" : issueCategory2.getCategoryID();

            if (ordinal1 == ordinal2)
                return id1.compareTo(id2);
            return ordinal1 - ordinal2;
        }
    }
}
