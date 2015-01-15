package org.jboss.windup.rules.apps.java.condition;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Condition that returns <code>true</code> if {@link SourceModeOption} is set to <code>true</code>.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class SourceMode extends GraphCondition
{
    /**
     * Create a new {@link SourceMode} condition that returns <code>true</code> if {@link SourceModeOption} is enabled.
     */
    public static ConditionBuilder isEnabled()
    {
        return new SourceMode(Boolean.TRUE);
    }

    /**
     * Create a new {@link SourceMode} condition that returns <code>true</code> if {@link SourceModeOption} is NOT
     * enabled.
     */
    public static ConditionBuilder isDisabled()
    {
        return new SourceMode(Boolean.FALSE);
    }

    private Boolean value;

    private SourceMode(Boolean value)
    {
        this.value = value;
    }

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context)
    {
        Boolean result = (Boolean) event.getGraphContext().getOptionMap().get(SourceModeOption.NAME);
        if (value)
        {
            return value.equals(result);
        }
        else
        {
            return value.equals(result) || result == null;
        }
    }

}
