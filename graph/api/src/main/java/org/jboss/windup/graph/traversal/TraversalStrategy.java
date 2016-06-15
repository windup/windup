package org.jboss.windup.graph.traversal;

/**
 * Allows for pluggable implementations of the {@link ProjectModelTraversal#getChildren()} method.
 *
 * See also the {@link AllTraversalStrategy}, {@link OnlyOnceTraversalStrategy}, {@link SharedLibsTraversalStrategy}.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public interface TraversalStrategy
{
    /**
     * Returns the child projects of the current project in the traversal.
     */
    Iterable<ProjectModelTraversal> getChildren(ProjectModelTraversal traversal);


    /**
     * Resets the state of this strategy, so it can be reused.
     * For instance, if the strategy is keeping some intermediate data like a set of visited projects etc.
     */
    public void reset();
}
