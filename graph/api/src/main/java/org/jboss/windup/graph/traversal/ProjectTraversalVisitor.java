package org.jboss.windup.graph.traversal;

/**
 * Implements a visitor pattern interface for the {@link ProjectModelTraversal}.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface ProjectTraversalVisitor {
    /**
     * This will be called for each {@link ProjectModelTraversal} in the traversal.
     */
    void visit(ProjectModelTraversal traversal);
}
