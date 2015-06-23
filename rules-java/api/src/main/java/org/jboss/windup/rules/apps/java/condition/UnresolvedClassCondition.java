package org.jboss.windup.rules.apps.java.condition;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.ast.java.data.ResolutionStatus;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.parameters.FrameContext;
import org.jboss.windup.config.parameters.FrameCreationContext;
import org.jboss.windup.config.parameters.ParameterizedGraphCondition;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryBuilderFrom;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.util.Maps;

import com.thinkaurelius.titan.core.attribute.Text;

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

    private boolean evaluateInternal(GraphRewrite event, EvaluationContext context)
    {
        Set<ResolutionStatus> unresolvedSet = new HashSet<>();
        unresolvedSet.add(ResolutionStatus.RECOVERED);
        unresolvedSet.add(ResolutionStatus.UNKNOWN);
        unresolvedSet.add(ResolutionStatus.UNRESOLVED);

        String inputVariableName = getInputVariablesName();
        QueryBuilderFrom query;
        if (StringUtils.isNotBlank(inputVariableName))
        {
            query = Query.from(inputVariableName);
            query.withProperty(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, JavaTypeReferenceModel.TYPE);
        }
        else
        {
            query = Query.fromType(JavaTypeReferenceModel.class);
        }
        String uuid = UUID.randomUUID().toString();
        query.as(uuid);
        try
        {
            query.withProperty(JavaTypeReferenceModel.RESOLUTION_STATUS, QueryPropertyComparisonType.CONTAINS_ANY_TOKEN, unresolvedSet);
            boolean result = query.evaluate(event, context);

            setResults(event, getOutputVariablesName(), Variables.instance(event).findVariable(uuid));
            return result;
        }
        finally
        {
            Variables.instance(event).removeVariable(uuid);
        }
    }
}
