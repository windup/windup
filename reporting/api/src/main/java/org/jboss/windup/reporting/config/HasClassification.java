package org.jboss.windup.reporting.config;

import java.util.HashSet;
import java.util.Set;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.iteration.AbstractIterationFilter;
import org.jboss.windup.graph.model.FileReferenceModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.service.ClassificationService;
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
public class HasClassification extends AbstractIterationFilter<WindupVertexFrame> implements Parameterized {
    private RegexParameterizedPatternParser titlePattern;

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context, WindupVertexFrame payload) {
        boolean result = false;
        ClassificationService service = new ClassificationService(event.getGraphContext());

        if (payload instanceof FileReferenceModel) {
            payload = ((FileReferenceModel) payload).getFile();
        }

        if (payload instanceof FileModel) {
            Iterable<ClassificationModel> classifications = service.getClassifications((FileModel) payload);
            if (titlePattern == null) {
                result = classifications.iterator().hasNext();
            } else {
                for (ClassificationModel c : classifications) {
                    ParameterizedPatternResult parseResult = titlePattern.parse(c.getClassification());
                    if (parseResult.matches() && parseResult.isValid(event, context)) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get the pattern for which this filter should match. (May be <code>null</code>.)
     */
    public String getTitlePattern() {
        if (titlePattern != null)
            return titlePattern.getPattern();
        else
            return null;
    }

    /**
     * Set the pattern for which this filter should match. (May be <code>null</code>.)
     */
    public void setTitlePattern(String titlePattern) {
        if (titlePattern != null)
            this.titlePattern = new RegexParameterizedPatternParser(titlePattern);
    }

    @Override
    public Set<String> getRequiredParameterNames() {
        Set<String> result = new HashSet<>();
        if (titlePattern != null)
            result.addAll(titlePattern.getRequiredParameterNames());
        return result;
    }

    @Override
    public void setParameterStore(ParameterStore store) {
        if (titlePattern != null)
            titlePattern.setParameterStore(store);
    }

}
