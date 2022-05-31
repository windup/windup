package org.jboss.windup.reporting.ruleexecution;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.ThemeProvider;

import java.util.List;

/**
 * Returns information about which {@link Rule}s have been evaluated by windup as well as their execution results.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class GetRuleExecutionResults implements WindupFreeMarkerMethod {

    public static final String NAME = "getRuleExecutionResults";

    private GraphRewrite event;

    @Override
    public String getMethodName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Takes a parameter of type " + AbstractRuleProvider.class.getSimpleName() + " and returns a List<"
                + RuleExecutionInformation.class.getSimpleName() + "> containing metadata related to the current " + ThemeProvider.getInstance().getTheme().getBrandNameLong() + " execution.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() != 1) {
            throw new TemplateModelException("Error, method expects one argument (AbstractRuleProvider)");
        }
        StringModel stringModelArg = (StringModel) arguments.get(0);
        AbstractRuleProvider ruleProvider = (AbstractRuleProvider) stringModelArg.getWrappedObject();
        List<RuleExecutionInformation> result = RuleExecutionResultsListener.instance(this.event).getRuleExecutionInformation(ruleProvider);
        ExecutionStatistics.get().end(NAME);
        return result;
    }

    @Override
    public void setContext(GraphRewrite event) {
        this.event = event;
    }

}
