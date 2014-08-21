package org.jboss.windup.config;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.ocpsoft.rewrite.config.CompositeOperation;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.spi.ConfigurationRuleBuilderInterceptor;

public class SimpleIterationConfigurationRuleBuilderInterceptor implements ConfigurationRuleBuilderInterceptor
{

    @Override
    public Operation perform(Operation operation)
    {
        return detectImplicitIteration(operation);
    }

    @Override
    public List<Operation> perform(List<Operation> list)
    {
        return detectImplicitIteration(list);
    }

    @Override
    public Operation otherwise(Operation operation)
    {
        return perform(operation);
    }

    @Override
    public List<Operation> otherwise(List<Operation> list)
    {
        return perform(list);
    }

    private List<Operation> detectImplicitIteration(List<Operation> list)
    {
        List<Operation> result = new ArrayList<>();

        if (list != null)
        {
            boolean requiresIteration = false;
            for (Operation operation : list)
            {
                if (requiresIteration(operation))
                {
                    requiresIteration = true;
                    break;
                }
            }

            if (requiresIteration)
            {
                result.add(Iteration.over().perform(list.toArray(new Operation[list.size()])).endIteration());
            }
            else
            {
                result = list;
            }
        }

        return result;
    }

    private Operation detectImplicitIteration(Operation operation)
    {
        if (requiresIteration(operation))
        {
            return Iteration.over().perform(operation).endIteration();
        }

        return operation;
    }

    private boolean requiresIteration(Operation operation)
    {
        if (operation instanceof Iteration)
        {
            return false;
        }

        if (operation instanceof AbstractIterationOperation)
            return true;

        if (operation instanceof CompositeOperation)
        {
            List<Operation> operations = ((CompositeOperation) operation).getOperations();
            for (Operation op : operations)
            {
                if (op instanceof AbstractIterationOperation)
                    return true;
                else if (!(op instanceof Iteration) && requiresIteration(op))
                    return true;
            }
        }

        return false;
    }

    /*
     * Things we don't care about for this interceptor
     */

    @Override
    public Condition when(Condition condition)
    {
        return condition;
    }

    @Override
    public List<Condition> when(List<Condition> list)
    {
        return list;
    }

    @Override
    public int withPriority(int priority)
    {
        return priority;
    }

    @Override
    public String withId(String id)
    {
        return id;
    }

    @Override
    public int priority()
    {
        return 0;
    }
}
