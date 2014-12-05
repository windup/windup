package org.jboss.windup.reporting.ruleexecution;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.forge.furnace.util.Assert;
import org.ocpsoft.rewrite.config.Rule;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * Contains information about a {@link Rule} and how it was executed by Windup (whether it was evaluated and executed, whether it failed, and various
 * statistics on what it performed.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 *
 */
public class RuleExecutionInformation
{
    private Rule rule;
    private boolean evaluationResult;
    private boolean executed;
    private boolean failed;
    private final AtomicInteger vertexIDsAdded = new AtomicInteger();
    private final AtomicInteger edgeIDsAdded = new AtomicInteger();
    private final AtomicInteger vertexIDsRemoved = new AtomicInteger();
    private final AtomicInteger edgeIDsRemoved = new AtomicInteger();

    private Throwable failureCause;

    /**
     * Create an instance for the provided rule.
     */
    public RuleExecutionInformation(Rule rule)
    {
        Assert.notNull(rule, "Rule object must not be null");
        this.rule = rule;
    }

    /**
     * Gets the {@link Rule}.
     */
    public Rule getRule()
    {
        return rule;
    }

    /**
     * Gets the result of the evaluation stage of the {@link Rule}. A false value here indicates that the "perform" function of the {@link Rule} would
     * not have executed.
     */
    public boolean getEvaluationResult()
    {
        return evaluationResult;
    }

    /**
     * Sets the result of the evaluation stage of the {@link Rule}. A false value here indicates that the "perform" function of the {@link Rule} would
     * not have executed.
     */
    void setEvaluationResult(boolean evaluationResult)
    {
        this.evaluationResult = evaluationResult;
    }

    /**
     * Gets an indication of whether or not this rule was executed by Windup.
     */
    public boolean isExecuted()
    {
        return executed;
    }

    /**
     * Sets the indication of whether or not this rule was executed by Windup.
     */
    void setExecuted(boolean executed)
    {
        this.executed = executed;
    }

    /**
     * Contains the failure status of the {@link Rule}.
     */
    public boolean isFailed()
    {
        return failed;
    }

    /**
     * Contains the failure status of the {@link Rule}.
     */
    void setFailed(boolean failed)
    {
        this.failed = failed;
    }

    /**
     * Contains the failure cause of the {@link Rule} (if any).
     */
    public Throwable getFailureCause()
    {
        return failureCause;
    }

    /**
     * Contains the failure cause of the {@link Rule} (if any).
     */
    void setFailureCause(Throwable failureCause)
    {
        this.failureCause = failureCause;
    }

    /**
     * Contains the IDs of any {@link Edge}s added by this {@link Rule}.
     */
    public int getEdgeIDsAdded()
    {
        return this.edgeIDsAdded.get();
    }

    /**
     * Contains the IDs of any {@link Vertex}s added by this {@link Rule}.
     */
    public int getVertexIDsAdded()
    {
        return this.vertexIDsAdded.get();
    }

    /**
     * Contains the IDs of any {@link Vertex} added by this {@link Rule}.
     */
    public void addVertexIDAdded(Object vID)
    {
        this.vertexIDsAdded.incrementAndGet();
    }

    /**
     * Contains the IDs of any {@link Edge}s added by this {@link Rule}.
     */
    public void addEdgeIDAdded(Object edgeID)
    {
        this.edgeIDsAdded.incrementAndGet();
    }

    /**
     * Contains the IDs of any {@link Edge}s removed by this {@link Rule}.
     */
    public int getEdgeIDsRemoved()
    {
        return this.edgeIDsRemoved.get();
    }

    /**
     * Contains the IDs of any {@link Vertex} removed by this {@link Rule}.
     */
    public int getVertexIDsRemoved()
    {
        return this.vertexIDsRemoved.get();
    }

    /**
     * Contains the IDs of any {@link Vertex} removed by this {@link Rule}.
     */
    public void addVertexIDRemoved(Object vID)
    {
        this.vertexIDsRemoved.incrementAndGet();
    }

    /**
     * Contains the IDs of any {@link Edge} removed by this {@link Rule}.
     */
    public void addEdgeIDRemoved(Object edgeID)
    {
        this.edgeIDsRemoved.incrementAndGet();
    }

    @Override
    public String toString()
    {
        return "RuleExecutionInformation [rule=" + rule + ", evaluationResult=" + evaluationResult + ", executed="
                    + executed + ", failed=" + failed + ", failureCause=" + failureCause + "]";
    }
}
