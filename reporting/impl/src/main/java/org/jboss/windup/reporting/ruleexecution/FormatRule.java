package org.jboss.windup.reporting.ruleexecution;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.config.RuleBuilder;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;

/**
 * Formats the provided rule for printing in an HTML report. This makes sure that the width of each Rule is not excessive, and also attempts to put in
 * linefeeds to make the rule easier to read.
 * 
 * This can be called from a Freemarker template as follows:
 * 
 * formatRule(Rule)
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 *
 */
public class FormatRule implements WindupFreeMarkerMethod
{
    private static final String NAME = "formatRule";
    private static final int MAX_WIDTH = 80;

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return "Takes a " + Rule.class.getSimpleName() + " as a paremeter and formats it into multiple rows for display.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() != 1)
        {
            throw new TemplateModelException("Error, method expects one argument (Rule)");
        }
        StringModel stringModelArg = (StringModel) arguments.get(0);
        Rule rule = (Rule) stringModelArg.getWrappedObject();

        ExecutionStatistics.get().end(NAME);
        return "addRule()" + serializeRule(rule, 0);
    }

    private String serializeRule(final Rule originalRule, int indentLevel)
    {
        if (!(originalRule instanceof RuleBuilder))
        {
            return wrap(originalRule.toString(), MAX_WIDTH, indentLevel);
        }
        final RuleBuilder rule = (RuleBuilder) originalRule;
        StringBuilder result = new StringBuilder();

        for (Condition condition : rule.getConditions())
        {
            String conditionToString = conditionToString(condition, indentLevel + 1);
            if (!conditionToString.isEmpty())
            {
                result.append("\n");
                insertPadding(result, indentLevel + 1);
                result.append(".when(" + wrap(conditionToString, MAX_WIDTH, indentLevel + 2) + ")");
            }

        }
        for (Operation operation : rule.getOperations())
        {
            String operationToString = operationToString(operation, indentLevel + 1);
            if (!operationToString.isEmpty())
            {
                result.append("\n");
                insertPadding(result, indentLevel + 1);
                result.append(".perform(" + wrap(operationToString, MAX_WIDTH, indentLevel + 2) + ")");
            }
        }
        if (rule.getId() != null && !rule.getId().isEmpty())
        {
            result.append("\n");
            insertPadding(result, indentLevel);
            result.append("withId(\"" + rule.getId() + "\")");
        }

        if (rule.priority() != 0)
        {
            result.append("\n");
            insertPadding(result, indentLevel);
            result.append(".withPriority(" + rule.priority() + ")");
        }

        return result.toString();
    }

    protected String conditionToString(Condition condition, int indentLevel)
    {
        if (condition instanceof RuleBuilder)
            return serializeRule((RuleBuilder) condition, indentLevel + 1);

        return condition == null ? "" : wrap(condition.toString(), MAX_WIDTH, indentLevel + 1);
    }

    protected String operationToString(Operation operation, int indentLevel)
    {
        if (operation instanceof RuleBuilder)
            return serializeRule((RuleBuilder) operation, indentLevel + 1);

        return operation == null ? "" : wrap(operation.toString(), MAX_WIDTH, indentLevel + 2);
    }

    private String wrap(String str, int wrapLength, int indentLevel)
    {
        StringBuilder result = new StringBuilder();
        try (StringReader sr = new StringReader(str))
        {
            BufferedReader br = new BufferedReader(sr);
            String line = null;
            try
            {
                while ((line = br.readLine()) != null)
                {
                    result.append(wrapLine(line, wrapLength, indentLevel)).append("\n");
                }
            }
            catch (IOException e)
            {
                throw new WindupException("Error... while reading a StringReader", e);
            }
        }
        return result.toString();
    }

    private String wrapLine(String str, int wrapLength, int indentLevel)
    {
        if (str == null)
        {
            return null;
        }
        if (wrapLength < 1)
        {
            wrapLength = 1;
        }
        int inputLineLength = str.length();
        int offset = 0;
        StringBuilder wrappedLine = new StringBuilder(inputLineLength + 32);

        StringBuilder filler = new StringBuilder();
        while ((inputLineLength - offset) > wrapLength)
        {
            if (str.charAt(offset) == '.')
            {
                offset++;
                filler.append('.');
                continue;
            }
            int spaceToWrapAt = str.lastIndexOf('.', wrapLength + offset);

            if (spaceToWrapAt >= offset)
            {
                // normal case
                if (wrappedLine.length() > 0)
                {
                    insertPadding(wrappedLine, indentLevel);
                }
                wrappedLine.append(filler);
                wrappedLine.append(str.substring(offset, spaceToWrapAt));
                wrappedLine.append(SystemUtils.LINE_SEPARATOR);
                offset = spaceToWrapAt;

            }
            else
            {
                // really long word or URL
                // wrap really long word one line at a time
                if (wrappedLine.length() > 0)
                {
                    insertPadding(wrappedLine, indentLevel);
                }
                wrappedLine.append(filler);
                wrappedLine.append(str.substring(offset, wrapLength + offset));
                wrappedLine.append(SystemUtils.LINE_SEPARATOR);
                offset += wrapLength;
            }
            filler.setLength(0);
        }

        // Whatever is left in line is short enough to just pass through
        if (wrappedLine.length() > 0)
        {
            insertPadding(wrappedLine, indentLevel);
        }
        wrappedLine.append(filler);
        wrappedLine.append(str.substring(offset));

        return wrappedLine.toString();
    }

    private void insertPadding(StringBuilder sb, int indentLevel)
    {
        for (int i = 0; i < indentLevel; i++)
        {
            sb.append("\t");
        }
    }

    @Override
    public void setContext(GraphRewrite event)
    {
    }
}
