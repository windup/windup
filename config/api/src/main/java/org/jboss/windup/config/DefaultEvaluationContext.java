package org.jboss.windup.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.context.ContextBase;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.context.RewriteState;
import org.ocpsoft.rewrite.param.DefaultParameterStore;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class DefaultEvaluationContext extends ContextBase implements EvaluationContext
{
    private final List<Operation> preOperations = new ArrayList<>();
    private final List<Operation> postOperations = new ArrayList<>();
    private RewriteState state;
    private final EvaluationContext parent;

    public DefaultEvaluationContext()
    {
        put(ParameterStore.class, new DefaultParameterStore());
        parent = null;
    }

    public DefaultEvaluationContext(EvaluationContext context)
    {
        put(ParameterStore.class, context.get(ParameterStore.class));
        put(ParameterValueStore.class, context.get(ParameterValueStore.class));
        parent = context;
    }

    @Override
    public boolean containsKey(Object key)
    {
        boolean result = super.containsKey(key);
        if (result == false && parent != null)
            result = parent.containsKey(key);
        return result;
    }

    @Override
    public Object get(Object key)
    {
        Object result = super.get(key);
        if (result == null && parent != null)
            result = parent.get(key);
        return result;
    }

    @Override
    public void addPreOperation(final Operation operation)
    {
        this.preOperations.add(operation);
    }

    @Override
    public void addPostOperation(final Operation operation)
    {
        this.preOperations.add(operation);
    }

    /**
     * Get an immutable view of the added pre-{@link Operation} instances.
     */
    public List<Operation> getPreOperations()
    {
        return Collections.unmodifiableList(preOperations);
    }

    /**
     * Get an immutable view of the added post-{@link Operation} instances.
     */
    public List<Operation> getPostOperations()
    {
        return Collections.unmodifiableList(postOperations);
    }

    @Override
    public String toString()
    {
        return "DefaultEvaluationContext [preOperations=" + preOperations + ", postOperations=" + postOperations + "]";
    }

    /**
     * Clears the state of this context so that it may be reused, saving instantiation cost during rule iteration.
     */
    public void clear()
    {
        this.postOperations.clear();
        this.postOperations.clear();
    }

    @Override
    public RewriteState getState()
    {
        return state;
    }

    public void setState(RewriteState state)
    {
        this.state = state;
    }
}