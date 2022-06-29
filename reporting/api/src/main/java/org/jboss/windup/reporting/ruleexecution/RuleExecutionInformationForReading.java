package org.jboss.windup.reporting.ruleexecution;

import org.ocpsoft.rewrite.config.Rule;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * Provides access to RuleExecutionInformation data
 *
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
public interface RuleExecutionInformationForReading {
    /**
     * Gets the {@link Rule}.
     */
    Rule getRule();

    /**
     * Gets the result of the evaluation stage of the {@link Rule}.
     * A false value here indicates that the "perform" function of the {@link Rule} would not have executed.
     */
    boolean getEvaluationResult();

    /**
     * Gets an indication of whether or not this rule was executed by Windup.
     */
    boolean isExecuted();

    /**
     * Contains the failure status of the {@link Rule}.
     */
    boolean isFailed();

    /**
     * Contains the failure cause of the {@link Rule} (if any).
     */
    Throwable getFailureCause();

    /**
     * Contains the IDs of any {@link Edge}s added by this {@link Rule}.
     */
    int getEdgeIDsAdded();

    /**
     * Contains the IDs of any {@link Vertex}s added by this {@link Rule}.
     */
    int getVertexIDsAdded();

    /**
     * Contains the IDs of any {@link Edge}s removed by this {@link Rule}.
     */
    int getEdgeIDsRemoved();

    /**
     * Contains the IDs of any {@link Vertex} removed by this {@link Rule}.
     */
    int getVertexIDsRemoved();
}
