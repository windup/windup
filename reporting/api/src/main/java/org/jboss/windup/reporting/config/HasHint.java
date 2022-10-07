package org.jboss.windup.reporting.config;

import java.util.HashSet;
import java.util.Set;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.iteration.AbstractIterationFilter;
import org.jboss.windup.graph.model.FileReferenceModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.service.InlineHintService;
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
public class HasHint extends AbstractIterationFilter<WindupVertexFrame> implements Parameterized {
    private RegexParameterizedPatternParser messagePattern;

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context, WindupVertexFrame payload) {
        ExecutionStatistics.get().begin(HasHint.class.getCanonicalName());
        try {
            boolean result = false;
            InlineHintService service = new InlineHintService(event.getGraphContext());

            if (payload instanceof FileReferenceModel) {
                Iterable<InlineHintModel> hints = service.getHintsForFileReference((FileReferenceModel) payload);
                if (messagePattern == null) {
                    result = hints.iterator().hasNext();
                } else {
                    for (InlineHintModel c : hints) {
                        ParameterizedPatternResult parseResult = messagePattern.parse(c.getHint());
                        if (parseResult.matches() && parseResult.isValid(event, context)) {
                            result = true;
                            break;
                        }
                    }
                }
            }

            if (payload instanceof FileModel) {
                Iterable<InlineHintModel> hints = service.getHintsForFile((FileModel) payload);
                if (messagePattern == null) {
                    result = hints.iterator().hasNext();
                } else {
                    for (InlineHintModel c : hints) {
                        ParameterizedPatternResult parseResult = messagePattern.parse(c.getHint());
                        if (parseResult.matches() && parseResult.isValid(event, context)) {
                            result = true;
                            break;
                        }
                    }
                }
            }
            return result;
        } finally {
            ExecutionStatistics.get().end(HasHint.class.getCanonicalName());
        }
    }

    /**
     * Get the pattern for which this filter should match. (May be <code>null</code>.)
     */
    public String getMessagePattern() {
        if (messagePattern != null)
            return messagePattern.getPattern();
        else
            return null;
    }

    /**
     * Set the pattern for which this filter should match. (May be <code>null</code>.)
     */
    public void setMessagePattern(String titlePattern) {
        if (titlePattern != null)
            this.messagePattern = new RegexParameterizedPatternParser(titlePattern);
    }

    @Override
    public Set<String> getRequiredParameterNames() {
        Set<String> result = new HashSet<>();
        if (messagePattern != null)
            result.addAll(messagePattern.getRequiredParameterNames());
        return result;
    }

    @Override
    public void setParameterStore(ParameterStore store) {
        if (messagePattern != null)
            messagePattern.setParameterStore(store);
    }

}
