package org.jboss.windup.addon.config.condition;

import java.util.Set;

import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.graph.model.resource.FileResource;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.Parameterized;
import org.ocpsoft.rewrite.param.ParameterizedPatternBuilder;
import org.ocpsoft.rewrite.param.ParameterizedPatternParser;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilenameMatchesCondition extends GraphCondition implements Parameterized
{

    private static final Logger LOG = LoggerFactory.getLogger(FilenameMatchesCondition.class);

    private final ParameterizedPatternParser pattern;

    public FilenameMatchesCondition(String pattern)
    {
        this.pattern = new RegexParameterizedPatternParser(pattern);
    }

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context)
    {
        ParameterizedPatternBuilder builder = pattern.getBuilder();
        if (builder.isParameterComplete(event, context))
        {
            LOG.debug("True.");
        }
        if (event.getResource() instanceof FileResource)
        {
            FileResource fileResource = (FileResource) event.getResource();

        }
        return false;
    }

    @Override
    public Set<String> getRequiredParameterNames()
    {
        return pattern.getRequiredParameterNames();
    }

    @Override
    public void setParameterStore(ParameterStore store)
    {
        pattern.setParameterStore(store);
    }
}
