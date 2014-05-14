package org.jboss.windup.addon.config;

import java.util.List;
import java.util.Set;

import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.common.services.ServiceLoader;
import org.ocpsoft.common.util.Iterators;
import org.ocpsoft.rewrite.bind.Evaluation;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.ConditionVisit;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.ConfigurationLoader;
import org.ocpsoft.rewrite.config.ConfigurationProvider;
import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.config.OperationVisit;
import org.ocpsoft.rewrite.config.ParameterizedCallback;
import org.ocpsoft.rewrite.config.ParameterizedConditionVisitor;
import org.ocpsoft.rewrite.config.ParameterizedOperationVisitor;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.param.ConfigurableParameter;
import org.ocpsoft.rewrite.param.DefaultParameter;
import org.ocpsoft.rewrite.param.Parameter;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.Parameterized;
import org.ocpsoft.rewrite.param.ParameterizedRule;
import org.ocpsoft.rewrite.util.Visitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphConfigurationLoader
{
    public static Logger LOG = LoggerFactory.getLogger(GraphConfigurationLoader.class);

    private final List<WindupConfigurationProvider> providers;

    @SuppressWarnings("unchecked")
    public GraphConfigurationLoader(Object context)
    {
        providers = GraphProviderSorter.sort(Iterators.asList(ServiceLoader.load(WindupConfigurationProvider.class)));
    }

    /**
     * Get a new {@link ConfigurationLoader} instance.
     */
    public static GraphConfigurationLoader create(final Object context)
    {
        return new GraphConfigurationLoader(context);
    }

    /**
     * Load all {@link ConfigurationProvider} instances, sort by {@link ConfigurationProvider#priority()}, and return a
     * unified, composite {@link Configuration} object.
     */
    public Configuration loadConfiguration(GraphContext context)
    {
        return build(context);
    }

    private Configuration build(GraphContext context)
    {

        ConfigurationBuilder result = ConfigurationBuilder.begin();

        for (WindupConfigurationProvider provider : providers)
        {
            Configuration cfg = provider.getConfiguration(context);
            List<Rule> list = cfg.getRules();
            for (final Rule rule : list)
            {
                result.addRule(rule);

                if (rule instanceof ParameterizedRule)
                {
                    ParameterizedCallback callback = new ParameterizedCallback()
                    {
                        @Override
                        public void call(Parameterized parameterized)
                        {
                            Set<String> names = parameterized.getRequiredParameterNames();
                            ParameterStore store = ((ParameterizedRule) rule).getParameterStore();

                            if (names != null)
                                for (String name : names)
                                {
                                    Parameter<?> parameter = store.get(name, new DefaultParameter(name));
                                    if (parameter instanceof ConfigurableParameter<?>)
                                        ((ConfigurableParameter<?>) parameter).bindsTo(Evaluation.property(name));
                                }

                            parameterized.setParameterStore(store);
                        }
                    };

                    Visitor<Condition> conditionVisitor = new ParameterizedConditionVisitor(callback);
                    new ConditionVisit(rule).accept(conditionVisitor);

                    Visitor<Operation> operationVisitor = new ParameterizedOperationVisitor(callback);
                    new OperationVisit(rule).accept(operationVisitor);
                }
            }
        }

        return result;
    }

}
