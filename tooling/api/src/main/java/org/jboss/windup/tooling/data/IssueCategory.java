package org.jboss.windup.tooling.data;

import java.io.Serializable;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface IssueCategory extends Serializable {
    String getCategoryID();

    void setCategoryID(String categoryID);

    String getOrigin();

    void setOrigin(String origin);

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    Integer getPriority();

    void setPriority(Integer priority);
}
