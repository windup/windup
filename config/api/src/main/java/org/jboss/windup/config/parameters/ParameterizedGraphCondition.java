package org.jboss.windup.config.parameters;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.forge.furnace.util.Iterators;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.DefaultParameterStore;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;
import org.ocpsoft.rewrite.param.Parameterized;

public abstract class ParameterizedGraphCondition extends GraphCondition implements Parameterized
{
    static final String PARAM_VALUE_STORE_MAP_KEY = ParameterizedGraphCondition.class.getName()
                + "_parameterValueStoreMap";
    static final String RESULT_VALUE_STORE_MAP_KEY = ParameterizedGraphCondition.class.getName()
                + "_resultParameterValueStoreMap";

    protected abstract String getVarname();

    protected abstract boolean evaluateWithValueStore(GraphRewrite event, EvaluationContext context, FrameContext frame);

    protected abstract boolean evaluateAndPopulateValueStores(GraphRewrite event, EvaluationContext context,
                FrameCreationContext frameCreationContext);

    @Override
    public final boolean evaluate(GraphRewrite event, final EvaluationContext context)
    {
        final Map<ParameterValueStore, Map<String, Iterable<? extends WindupVertexFrame>>> valueStores = getParameterValueStoreMap(context);
        Map<WindupVertexFrame, ParameterValueStore> resultSetStores = getResultValueStoreMap(context);

        ParameterValueStore previousValueStore = DefaultParameterValueStore.getInstance(context);
        try
        {
            if (valueStores.isEmpty())
            {
                FrameCreationContext frameCreationContext = new FrameCreationContext()
                {
                    private ParameterValueStore current;
                    private DefaultParameterValueStore original = (DefaultParameterValueStore) DefaultParameterValueStore
                                .getInstance(context);

                    @Override
                    public void beginNew(Map<String, Iterable<? extends WindupVertexFrame>> variables)
                    {
                        ParameterValueStore clone = clone(original);
                        this.current = clone;
                        context.put(ParameterValueStore.class, clone);

                        if (variables == null)
                            variables = new LinkedHashMap<String, Iterable<? extends WindupVertexFrame>>();
                        valueStores.put(clone, variables);
                    }

                    private ParameterValueStore clone(DefaultParameterValueStore instance)
                    {
                        DefaultParameterValueStore clone = new DefaultParameterValueStore(instance);
                        return clone;
                    }

                    @Override
                    public void rollback()
                    {
                        if (current != null)
                            valueStores.remove(current);
                    }
                };

                try
                {
                    return evaluateAndPopulateValueStores(event, context, frameCreationContext);
                }
                finally
                {
                    for (Entry<ParameterValueStore, Map<String, Iterable<? extends WindupVertexFrame>>> entry : valueStores
                                .entrySet())
                    {
                        ParameterValueStore valueStore = entry.getKey();

                        Map<String, Iterable<? extends WindupVertexFrame>> layer = entry.getValue();
                        if (layer == null)
                        {
                            throw new WindupException("Value store with no associated variables frame. This should not happen");
                        }

                        Iterable<? extends WindupVertexFrame> variable = layer.get(getVarname());
                        if (variable != null)
                        {
                            for (WindupVertexFrame frame : variable)
                            {
                                ParameterValueStore last = resultSetStores.put(frame, valueStore);
                                if (last != null)
                                {
                                    // WHY DOES THIS HAPPEN?
                                    System.out.println("DOES THIS STILL HAPPEN?");
                                }
                            }
                        }
                    }
                }
            }
            else
            {
                Set<WindupVertexFrame> resultSet = new LinkedHashSet<>();
                boolean result = false;

                DefaultParameterValueStore original = (DefaultParameterValueStore) DefaultParameterValueStore
                            .getInstance(context);

                for (Entry<ParameterValueStore, Map<String, Iterable<? extends WindupVertexFrame>>> entry : valueStores
                            .entrySet())
                {
                    ParameterValueStore valueStore = entry.getKey();
                    ParameterStore store = DefaultParameterStore.getInstance(context);
                    Map<String, Iterable<? extends WindupVertexFrame>> variables = entry.getValue();
                    try
                    {
                        Variables.instance(event).push(variables);
                        final AtomicBoolean rejected = new AtomicBoolean(false);
                        FrameContext frameContext = new FrameContext()
                        {
                            @Override
                            public void reject()
                            {
                                rejected.set(true);
                            }
                        };

                        try
                        {
                            context.put(ParameterValueStore.class, valueStore);

                            /*
                             * Each ValueStore must correspond with the variables map which which it was created.
                             */
                            if (evaluateWithValueStore(event, context, frameContext))
                                result = true;

                            if (rejected.get())
                                valueStores.remove(valueStore);
                        }
                        finally
                        {
                            context.put(ParameterValueStore.class, original);
                        }
                    }
                    finally
                    {
                        Iterable<? extends WindupVertexFrame> variable = Variables.instance(event).findVariable(getVarname());
                        if (variable != null)
                        {
                            resultSet.addAll(Iterators.asSet(variable));
                            for (WindupVertexFrame frame : variable)
                            {
                                ParameterValueStore last = resultSetStores.put(frame, valueStore);
                                if (last != null)
                                {
                                    // TODO is this a valid scenario?
                                }

                            }
                        }
                        Variables.instance(event).pop();
                    }
                }
                Variables.instance(event).setVariable(getVarname(), resultSet);
                return result;
            }
        }
        finally
        {
            context.put(ParameterValueStore.class, previousValueStore);
            context.put(PARAM_VALUE_STORE_MAP_KEY, valueStores);
            context.put(RESULT_VALUE_STORE_MAP_KEY, resultSetStores);
        }
    }

    @SuppressWarnings("unchecked")
    static Map<ParameterValueStore, Map<String, Iterable<? extends WindupVertexFrame>>> getParameterValueStoreMap(
                final EvaluationContext context)
    {
        Map<ParameterValueStore, Map<String, Iterable<? extends WindupVertexFrame>>> cachedStores = (Map<ParameterValueStore, Map<String, Iterable<? extends WindupVertexFrame>>>) context
                    .get(PARAM_VALUE_STORE_MAP_KEY);
        Map<ParameterValueStore, Map<String, Iterable<? extends WindupVertexFrame>>> valueStores = cachedStores;
        if (valueStores == null)
            valueStores = new ConcurrentHashMap<ParameterValueStore, Map<String, Iterable<? extends WindupVertexFrame>>>();

        return valueStores;
    }

    @SuppressWarnings("unchecked")
    static Map<WindupVertexFrame, ParameterValueStore> getResultValueStoreMap(EvaluationContext context)
    {
        Map<WindupVertexFrame, ParameterValueStore> result = (Map<WindupVertexFrame, ParameterValueStore>) context
                    .get(RESULT_VALUE_STORE_MAP_KEY);
        if (result == null)
        {
            result = new LinkedHashMap<>();
        }
        return result;
    }

}
