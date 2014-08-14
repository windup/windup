package org.jboss.windup.config.operation;

import java.util.Arrays;
import java.util.List;

import org.ocpsoft.rewrite.config.NoOp;
import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.config.OperationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;

/**
 * Windup implementation of the {@link OperationBuilder}.
 * @author mbriskar
 *
 */
public class WindupOperationBuilder implements PayLoadVariableNameHolder,OperationBuilder
{

    private final Operation left;
    private final Operation right;

    public WindupOperationBuilder(final Operation left, final Operation right)
    {
       this.left = left;
       this.right = right;
    }
    
    @Override
    public OperationBuilder and(final Operation other)
    {
       if (other == null)
          return this;
       return new WindupOperationBuilder(this, other);
    }

    @Override
    public void perform(final Rewrite event, final EvaluationContext context)
    {
       left.perform(event, context);
       right.perform(event, context);
    }

    public List<Operation> getOperations()
    {
       return Arrays.asList(left, right);
    }

    @Override
    public String toString()
    {
       if (left instanceof NoOp)
          return "" + right;

       return left + ".and(" + right + ")";
    }

    @Override
    public void setVariableName(String variable)
    {
        if(left instanceof PayLoadVariableNameHolder) {
            PayLoadVariableNameHolder leftHolder=(PayLoadVariableNameHolder) left;
            if(!leftHolder.hasVariableNameSet()) {
                leftHolder.setVariableName(variable);
            }
        }
        if(right instanceof PayLoadVariableNameHolder) {
            PayLoadVariableNameHolder rightHolder=(PayLoadVariableNameHolder) right;
            if(!rightHolder.hasVariableNameSet())  {
                rightHolder.setVariableName(variable);
            }
        }
    }

    /**
     * Returns if all of the child's that are {@PayLoadVariableNameHolder}
     */
    @Override
    public boolean hasVariableNameSet()
    {
        boolean result =true;
        if(left instanceof PayLoadVariableNameHolder) {
            PayLoadVariableNameHolder leftHolder=(PayLoadVariableNameHolder) left;
            result= result && leftHolder.hasVariableNameSet();
        }
        if(right instanceof PayLoadVariableNameHolder) {
            PayLoadVariableNameHolder rightHolder=(PayLoadVariableNameHolder) right;
            result= result && rightHolder.hasVariableNameSet();
        }
        return result;
    }

}
