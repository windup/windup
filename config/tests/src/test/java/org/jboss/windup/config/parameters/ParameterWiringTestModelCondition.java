package org.jboss.windup.config.parameters;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryBuilderFrom;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.DefaultParameterStore;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.Parameterized;
import org.ocpsoft.rewrite.param.ParameterizedPatternResult;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;
import org.ocpsoft.rewrite.util.Maps;

public class ParameterWiringTestModelCondition extends ParameterizedGraphCondition implements Parameterized
{
    private RegexParameterizedPatternParser pattern;
    private String varname = Iteration.DEFAULT_VARIABLE_LIST_STRING;
    private String fromVarname;

    public ParameterWiringTestModelCondition(String pattern)
    {
        this.pattern = new RegexParameterizedPatternParser(pattern);
    }

    public static ParameterWiringTestModelCondition matchesValue(String pattern)
    {
        return new ParameterWiringTestModelCondition(pattern);
    }

    public ParameterWiringTestModelCondition from(String varname)
    {
        this.fromVarname = varname;
        return this;
    }

    public ConditionBuilder as(String varname)
    {
        this.varname = varname;
        return this;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected boolean evaluateAndPopulateValueStores(GraphRewrite event, EvaluationContext context,
                FrameCreationContext frameCreationContext)
    {
        ParameterStore store = DefaultParameterStore.getInstance(context);

        QueryBuilderFrom query = Query.fromType(ParameterWiringTestModel.class);
        if (!StringUtils.isBlank(fromVarname))
        {
            query = Query.from(fromVarname);
        }

        Pattern compiledPattern = pattern.getCompiledPattern(store);
        query.withProperty(ParameterWiringTestModel.VALUE, QueryPropertyComparisonType.REGEX,
                    compiledPattern.pattern());

        String uuid = UUID.randomUUID().toString();
        query.as(uuid);

        List<WindupVertexFrame> allFrameResults = new ArrayList<>();
        if (query.evaluate(event, context))
        {
            Iterable<? extends WindupVertexFrame> frames = Variables.instance(event).findVariable(uuid);
            for (WindupVertexFrame frame : frames)
            {
                ParameterWiringTestModel model = (ParameterWiringTestModel) frame;

                ParameterizedPatternResult parseResult = pattern.parse(model.getValue());
                if (parseResult.matches())
                {
                    Map<String, List<WindupVertexFrame>> variables = new LinkedHashMap<String, List<WindupVertexFrame>>();
                    frameCreationContext.beginNew((Map) variables);
                    if (parseResult.submit(event, context))
                    {
                        allFrameResults.add(model);
                        Maps.addListValue(variables, varname, model);
                    }
                    else
                    {
                        System.out.println("nope: " + model);
                        frameCreationContext.rollback();
                    }
                }
                else
                {
                    System.out.println("nope: " + model);
                }
            }
            Variables.instance(event).removeVariable(uuid);
            Variables.instance(event).setVariable(varname, allFrameResults);
            return true;
        }

        return false;
    }

    @Override
    protected boolean evaluateWithValueStore(GraphRewrite event, EvaluationContext context,
                FrameContext frameContext)
    {
        ParameterStore store = DefaultParameterStore.getInstance(context);

        QueryBuilderFrom query = Query.fromType(ParameterWiringTestModel.class);
        if (!StringUtils.isBlank(fromVarname))
        {
            query = Query.from(fromVarname);
        }

        Pattern compiledPattern = pattern.getCompiledPattern(store);
        query.withProperty(ParameterWiringTestModel.VALUE, QueryPropertyComparisonType.REGEX,
                    compiledPattern.pattern());

        String uuid = UUID.randomUUID().toString();
        query.as(uuid);
        if (query.evaluate(event, context))
        {
            boolean result = false;
            List<WindupVertexFrame> results = new ArrayList<>();
            Iterable<? extends WindupVertexFrame> frames = Variables.instance(event).findVariable(uuid);
            for (WindupVertexFrame frame : frames)
            {
                ParameterWiringTestModel model = (ParameterWiringTestModel) frame;

                String value = model.getValue();
                ParameterizedPatternResult parseResult = pattern.parse(value);
                if (parseResult.submit(event, context))
                {
                    result = true;
                    results.add(model);
                }
            }

            Variables.instance(event).removeVariable(uuid);
            if (result)
            {
                Variables.instance(event).setVariable(varname, results);
                return true;
            }
        }

        frameContext.reject();
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

    @Override
    protected String getVarname()
    {
        return varname;
    }

}