package org.jboss.windup.reporting.category;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>
 * Windup's {@link IssueCategory} class provides a way of organizing code issues into groups according to their relative importance.
 * </p>
 * <p>
 * Example categories include the following:
 * <ul>
 * <li>Migration - Mandatory</li>
 * <li>Migration - Potential</li>
 * <li>Security</li>
 * <li>Modernization</li>
 * </ul>
 *
 * <p>
 * The categories themselves are defined by the rulesets and are not limited to the above list of items.
 * </p>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class IssueCategory {
    @XmlElement(name = "category-id")
    private String categoryID;
    @XmlElement
    private String origin;
    @XmlElement
    private String name;
    @XmlElement
    private String description;
    @XmlElement
    private Integer priority;

    boolean placeholder = false;

    /**
     * Creates a new {@link IssueCategory} with the provided parameters.
     */
    public IssueCategory(String categoryID, String origin, String name, String description, Integer priority) {
        this.categoryID = categoryID;
        this.origin = origin;
        this.name = name;
        this.description = description;
        this.priority = priority;
    }

    /**
     * Creates a new {@link IssueCategory} with the provided parameters.
     */
    IssueCategory(String categoryID, String origin, String name, String description, Integer priority, boolean placeholder) {
        this.categoryID = categoryID;
        this.origin = origin;
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.placeholder = placeholder;
    }

    /**
     * Creates an in-memory {@link IssueCategory} based on the provided model.
     */
    public IssueCategory(IssueCategoryModel issueCategoryModel) {
        this.categoryID = issueCategoryModel.getCategoryID();
        this.origin = issueCategoryModel.getOrigin();
        this.name = issueCategoryModel.getName();
        this.description = issueCategoryModel.getDescription();
        this.priority = issueCategoryModel.getPriority();
    }

    /**
     * Just here to support modular classloading.
     */
    public IssueCategory() {

    }

    /**
     * Indicates that this item is just a placeholder that is expected to be replaced by one from the XML rules.
     */
    public boolean isPlaceholder() {
        return placeholder;
    }

    /**
     * Contains a unique identifier for this {@link IssueCategory}.
     */
    public String getCategoryID() {
        return categoryID;
    }

    /**
     * Contains the original path to where this {@link IssueCategory} was defined.
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * Contains a human readable name for this {@link IssueCategory}.
     */
    public String getName() {
        return name;
    }

    /**
     * Contains a human readable description for this {@link IssueCategory}.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Contains a priority value that can be used for ordering things in reports.
     */
    public Integer getPriority() {
        return priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof IssueCategory))
            return false;

        IssueCategory that = (IssueCategory) o;

        return categoryID != null ? categoryID.equals(that.categoryID) : that.categoryID == null;

    }

    @Override
    public int hashCode() {
        return categoryID != null ? categoryID.hashCode() : 0;
    }
}
