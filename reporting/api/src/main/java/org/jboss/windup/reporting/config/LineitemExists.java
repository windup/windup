/**
 * 
 */
package org.jboss.windup.reporting.config;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryBuilderFind;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.reporting.model.OverviewReportLineMessageModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * @author mnovotny
 *
 */
public class LineitemExists extends GraphCondition {
	
    private String messagePattern;

    private LineitemExists(String messagePattern)
    {
        this.messagePattern = messagePattern;
    }

	@Override
	public boolean evaluate(GraphRewrite event, EvaluationContext context) {
		QueryBuilderFind q = Query.fromType(OverviewReportLineMessageModel.class);
        q.withProperty(OverviewReportLineMessageModel.PROPERTY_MESSAGE, QueryPropertyComparisonType.REGEX, messagePattern);
        return q.evaluate(event, context);
	}

    /**
     * Use the given message regular expression to match against {@link InlineHintModel#getHint()} property.
     */
    public static LineitemExists withMessage(String messagePattern)
    {
        return new LineitemExists(messagePattern);
    }
}
