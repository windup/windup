package org.jboss.windup.graph.model;

/**
 * Indicates that this is associated with a specific project.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface HasProject {
    /**
     * Gets the Project that is directly associated with this item.
     */
    ProjectModel getProjectModel();
}
