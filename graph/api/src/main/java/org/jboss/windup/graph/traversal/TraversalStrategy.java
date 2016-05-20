package org.jboss.windup.graph.traversal;

/**
 * Allows for pluggable implementations of the {@link ProjectModelTraversal#getChildren()} method.
 *
 * See also the {@link AllTraversalStrategy}.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface TraversalStrategy
{
    /**
     * Returns the child projects of the current project in the traversal.
     */
    Iterable<ProjectModelTraversal> getChildren(ProjectModelTraversal traversal);
}
