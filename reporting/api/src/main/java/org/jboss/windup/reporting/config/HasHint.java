package org.jboss.windup.reporting.config;

import java.util.HashSet;
import java.util.Set;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.iteration.AbstractIterationFilter;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.rules.files.model.FileLocationModel;
import org.jboss.windup.rules.files.model.FileReferenceModel;
import org.jboss.windup.util.ExecutionStatistics;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.Parameterized;
import org.ocpsoft.rewrite.param.ParameterizedPatternResult;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;

/**
 * An implementation of {@link AbstractIterationFilter} to filter models based on the existence of a
 * {@link ClassificationModel} attached to the given payload.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public class HasHint extends AbstractIterationFilter<WindupVertexFrame> implements Parameterized
{
    private RegexParameterizedPatternParser messagePattern;

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context, WindupVertexFrame payload)
    {
        ExecutionStatistics.get().begin(HasHint.class.getCanonicalName());
        try
        {
            InlineHintService service = new InlineHintService(event.getGraphContext());

            Iterable<InlineHintModel> hints = null;
            // A fake FileLocationModel with [1,1] row and column, so we want hints for the whole file.
            if (payload instanceof FileLocationModel && Boolean.TRUE.equals(((FileLocationModel) payload).isSpansWholeFile()))
                hints = service.getHintsForFile(((FileLocationModel) payload).getFile());
            // FileReferenceModel is a particular position in a file -> match only hints for that line.
            else if (payload instanceof FileReferenceModel)
                hints = service.getHintsForFileReference((FileReferenceModel) payload);
            else if (payload instanceof FileModel)
                hints = service.getHintsForFile((FileModel) payload);

            if (hints == null)
                return false;

            if (messagePattern == null)
                return hints.iterator().hasNext();
            for (InlineHintModel c : hints)
            {
                ParameterizedPatternResult parseResult = messagePattern.parse(c.getHint());
                if (parseResult.matches() && parseResult.isValid(event, context))
                    return true;
            }

            return false;
        }
        finally
        {
            ExecutionStatistics.get().end(HasHint.class.getCanonicalName());
        }
    }

    /**
     * Get the pattern for which this filter should match. (May be <code>null</code>.)
     */
    public String getMessagePattern()
    {
        if (messagePattern != null)
            return messagePattern.getPattern();
        else
            return null;
    }

    /**
     * Set the pattern for which this filter should match. (May be <code>null</code>.)
     */
    public void setMessagePattern(String titlePattern)
    {
        if (titlePattern != null)
            this.messagePattern = new RegexParameterizedPatternParser(titlePattern);
    }

    @Override
    public Set<String> getRequiredParameterNames()
    {
        Set<String> result = new HashSet<>();
        if (messagePattern != null)
            result.addAll(messagePattern.getRequiredParameterNames());
        return result;
    }

    @Override
    public void setParameterStore(ParameterStore store)
    {
        if (messagePattern != null)
            messagePattern.setParameterStore(store);
    }

}
