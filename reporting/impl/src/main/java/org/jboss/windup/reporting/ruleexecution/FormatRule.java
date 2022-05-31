package org.jboss.windup.reporting.ruleexecution;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleUtils;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.util.ExecutionStatistics;
import org.ocpsoft.rewrite.config.Rule;

import java.util.List;

/**
 * Formats the provided rule for printing in an HTML report. This makes sure that the width of each Rule is not excessive, and also attempts to put in
 * linefeeds to make the rule easier to read.
 * <p>
 * This can be called from a Freemarker template as follows:
 * <p>
 * formatRule(Rule)
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class FormatRule implements WindupFreeMarkerMethod {
    private static final String NAME = "formatRule";

    @Override
    public String getMethodName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Takes a " + Rule.class.getSimpleName() + " as a paremeter and formats it into multiple rows for display.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() != 1) {
            throw new TemplateModelException("Error, method expects one argument (Rule)");
        }
        StringModel stringModelArg = (StringModel) arguments.get(0);
        Rule rule = (Rule) stringModelArg.getWrappedObject();

        String result = RuleUtils.ruleToRuleContentsString(rule, 0);

        ExecutionStatistics.get().end(NAME);
        return result;
    }

    @Override
    public void setContext(GraphRewrite event) {
    }
}
