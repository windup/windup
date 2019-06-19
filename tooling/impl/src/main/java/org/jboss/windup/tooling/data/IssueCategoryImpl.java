package org.jboss.windup.tooling.data;

import org.jboss.windup.reporting.category.IssueCategoryModel;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class IssueCategoryImpl implements IssueCategory
{
    private static final long serialVersionUID = 1L;

    private String categoryID;
    private String origin;
    private String name;
    private String description;
    private Integer priority;

    public IssueCategoryImpl(IssueCategoryModel issueCategoryModel)
    {
        this.categoryID = issueCategoryModel.getCategoryID();
        this.origin = issueCategoryModel.getOrigin();
        this.name = issueCategoryModel.getName();
        this.description = issueCategoryModel.getDescription();
        this.priority = issueCategoryModel.getPriority();
    }

    @Override
    public String getCategoryID()
    {
        return categoryID;
    }

    @Override
    public void setCategoryID(String categoryID)
    {
        this.categoryID = categoryID;
    }

    @Override
    public String getOrigin()
    {
        return origin;
    }

    @Override
    public void setOrigin(String origin)
    {
        this.origin = origin;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    @Override
    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public Integer getPriority()
    {
        return priority;
    }

    @Override
    public void setPriority(Integer priority)
    {
        this.priority = priority;
    }
}
