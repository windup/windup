package org.jboss.windup.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.ocpsoft.rewrite.config.CompositeOperation;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.config.Perform;
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
                return operationsToIterationOperations(list);
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
            final List<Operation> allOperations = flattenOperations(Collections.singletonList(operation));
            final List<Operation> operationsWrappedInIterators = operationsToIterationOperations(allOperations);

            return Perform.all(operationsWrappedInIterators.toArray(new Operation[operationsWrappedInIterators.size()]));
        }

        return operation;
    }

    private List<Operation> operationsToIterationOperations(List<Operation> operations)
    {
        List<Operation> results = new ArrayList<>();

        LinkedHashMap<String, List<Operation>> operationMap = new LinkedHashMap<>();
        for (Operation operation : operations)
        {
            String expectedInputVarName = null;
            if (operation instanceof AbstractIterationOperation)
            {
                expectedInputVarName = ((AbstractIterationOperation<?>) operation).getInputVariableName();
            }
            List<Operation> operationsForInputVar = operationMap.get(expectedInputVarName);
            if (operationsForInputVar == null)
            {
                operationsForInputVar = new ArrayList<>();
                operationMap.put(expectedInputVarName, operationsForInputVar);
            }
            operationsForInputVar.add(operation);
        }
        for (Map.Entry<String, List<Operation>> operationMapEntry : operationMap.entrySet())
        {
            String inputVarName = operationMapEntry.getKey();
            List<Operation> ops = operationMapEntry.getValue();
            if (inputVarName == null)
            {
                results.add(Iteration.over().perform(ops.toArray(new Operation[ops.size()])).endIteration());
            }
            else
            {
                results.add(Iteration.over(inputVarName).perform(ops.toArray(new Operation[ops.size()])).endIteration());
            }
        }
        return results;
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

    private static List<Operation> flattenOperations(List<Operation> operations)
    {
        List<Operation> result = new ArrayList<>();
        for (Operation operation : operations)
        {
            if (operation instanceof Iteration)
            {
                result.add(operation);
            }
            else if (operation instanceof CompositeOperation)
            {
                List<Operation> compositeOperations = ((CompositeOperation) operation).getOperations();
                result.addAll(flattenOperations(compositeOperations));
            }
            else
            {
                result.add(operation);
            }
        }
        return result;
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
