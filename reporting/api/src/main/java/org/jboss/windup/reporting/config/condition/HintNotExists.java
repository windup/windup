package org.jboss.windup.reporting.config.condition;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.rules.files.model.FileLocationModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Does exactly the opposite as {@link HintExists}
 */
public class HintNotExists extends HintExists
{
    private HintNotExists(String messagePattern)
    {
        super(messagePattern);
    }

    @Override public boolean accept(GraphRewrite event, EvaluationContext context, FileLocationModel payload) {
        return !super.accept(event,context,payload);
    }

    /**
     * Use the given message regular expression to match against {@link InlineHintModel#getHint()} property.
     */
    public static HintNotExists withMessage(String messagePattern)
    {
        return new HintNotExists(messagePattern);
    }
}
