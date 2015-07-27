package org.jboss.windup.rules.apps.java.condition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.ast.java.data.ResolutionStatus;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.parameters.FrameContext;
import org.jboss.windup.config.parameters.FrameCreationContext;
import org.jboss.windup.config.parameters.ParameterizedGraphCondition;
import org.jboss.windup.graph.frames.VertexFromFramedIterable;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.util.Maps;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * Finds all {@link JavaTypeReferenceModel} instances for which we were not definitely able to find a binding.
 *
 * This could indicate missing classes on the classpath.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class UnresolvedClassCondition extends ParameterizedGraphCondition
{
    @Override
    protected String getVarname()
    {
        return getOutputVariablesName();
    }

    @Override
    protected boolean evaluateWithValueStore(GraphRewrite event, EvaluationContext context, FrameContext frame)
    {
        return evaluateInternal(event, context);
    }

    @Override
    protected boolean evaluateAndPopulateValueStores(GraphRewrite event, EvaluationContext context, FrameCreationContext frameCreationContext)
    {
        boolean result = evaluateInternal(event, context);
        if (result)
        {
            LinkedHashMap<String, List<WindupVertexFrame>> variables = new LinkedHashMap<>();
            frameCreationContext.beginNew((Map) variables);

            // make sure value stores are populated
            for (WindupVertexFrame frame : Variables.instance(event).findVariable(getOutputVariablesName()))
            {
                Maps.addListValue(variables, getOutputVariablesName(), frame);
            }
        }
        return result;
    }

    @Override
    public Set<String> getRequiredParameterNames()
    {
        return Collections.emptySet();
    }

    @Override
    public void setParameterStore(ParameterStore store)
    {
        // noop
    }

    private boolean evaluateInternal(final GraphRewrite event, EvaluationContext context)
    {
        String inputVariableName = getInputVariablesName();
        final GremlinPipeline<Vertex, Vertex> pipeline;
        if (StringUtils.isNotBlank(inputVariableName))
        {
            Iterable<? extends WindupVertexFrame> inputFrames = Variables.instance(event).findVariable(inputVariableName);
            pipeline = new GremlinPipeline<>(new VertexFromFramedIterable(inputFrames));
        }
        else
        {
            pipeline = new GremlinPipeline<>(event.getGraphContext().getGraph());
            pipeline.V();
            pipeline.has(WindupVertexFrame.TYPE_PROP, JavaTypeReferenceModel.TYPE);
        }
        pipeline.hasNot(JavaTypeReferenceModel.RESOLUTION_STATUS, ResolutionStatus.RESOLVED);

        List<WindupVertexFrame> results = new ArrayList<>();
        boolean result = false;
        for (Vertex vertex : pipeline)
        {
            if (!result)
                result = true;
            results.add(event.getGraphContext().getFramed().frame(vertex, WindupVertexFrame.class));
        }

        setResults(event, getOutputVariablesName(), results);
        return result;
    }
}
