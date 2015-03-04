package org.jboss.windup.config.loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.forge.furnace.proxy.Proxies;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.util.exception.WindupMultiStringException;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

/**
 * Sorts {@link RuleProvider}s based upon their executeBefore/executeAfter methods.
 *
 * @author jsightler <jesse.sightler@gmail.com>
 * @author Ondrej Zizka <zizka@seznam.com>
 */
public class WindupRuleProviderSorter
{
    /**
     * All {@link RuleProvider}s
     */
    private List<RuleProvider> providers;

    /**
     * Maps from the RuleProvider class back to the instance of RuleProvider
     */
    private final IdentityHashMap<Class<? extends RuleProvider>, RuleProvider> classToProviderMap = new IdentityHashMap<>();

    /**
     * Maps from the provider's ID to the RuleProvider
     */
    private final Map<String, RuleProvider> idToProviderMap = new HashMap<>();

    private WindupRuleProviderSorter(List<RuleProvider> providers)
    {
        this.providers = new ArrayList<>(providers);
        initializeLookupCaches();
        sort();
    }

    /**
     * Sort the provided list of {@link RuleProvider}s and return the result.
     */
    public static List<RuleProvider> sort(List<RuleProvider> providers)
    {
        WindupRuleProviderSorter sorter = new WindupRuleProviderSorter(providers);
        return sorter.getProviders();
    }

    /**
     * Gets the provider list
     */
    private List<RuleProvider> getProviders()
    {
        return providers;
    }

    /**
     * Initializes lookup caches that are used during sort to lookup providers by ID or Java {@link Class}.
     */
    private void initializeLookupCaches()
    {
        // Initialize lookup maps
        for (RuleProvider provider : providers)
        {
            Class<? extends RuleProvider> unproxiedClass = unwrapType(provider.getClass());
            classToProviderMap.put(unproxiedClass, provider);
            idToProviderMap.put(provider.getMetadata().getID(), provider);
        }
    }

    /**
     * Perform the entire sort operation
     */
    private void sort()
    {
        // Build a directed graph based upon the dependencies
        DefaultDirectedWeightedGraph<RuleProvider, DefaultEdge> g = new DefaultDirectedWeightedGraph<>(
                    DefaultEdge.class);

        // Add initial vertices to the graph
        // Initialize lookup maps
        for (RuleProvider provider : providers)
        {
            g.addVertex(provider);
        }

        addProviderRelationships(g);

        checkForCycles(g);

        // create the final results list
        List<RuleProvider> result = new ArrayList<RuleProvider>(this.providers.size());
        // use topological ordering to make it all the right order
        TopologicalOrderIterator<RuleProvider, DefaultEdge> iterator = new TopologicalOrderIterator<>(g);
        while (iterator.hasNext())
        {
            RuleProvider provider = iterator.next();
            result.add(provider);
        }

        this.providers = Collections.unmodifiableList(result);

        int index = 0;
        for (RuleProvider provider : this.providers)
        {
            if (provider instanceof AbstractRuleProvider)
                ((AbstractRuleProvider) provider).setExecutionIndex(index++);
        }
    }

    /**
     * Add edges between {@link WinduPRuleProvider}s based upon their dependency relationships.
     */
    private void addProviderRelationships(DefaultDirectedWeightedGraph<RuleProvider, DefaultEdge> g)
    {
        linkRulePhases();

        for (RuleProvider provider : providers)
        {
            RuleProvider phaseProvider = getByClass(provider.getMetadata().getPhase());

            List<String> errors = new LinkedList<>();

            // add connections to ruleproviders that should execute before this one
            for (Class<? extends RuleProvider> clz : provider.getMetadata().getExecuteAfter())
            {
                addExecuteAfterRelationship(g, provider, errors, clz);
            }

            if (phaseProvider != null)
            {
                if (provider.getMetadata().getPhase() != Proxies.unwrap(provider).getClass())
                    addExecuteAfterRelationship(g, provider, errors, provider.getMetadata().getPhase());

                for (Class<? extends RuleProvider> clz : phaseProvider.getMetadata().getExecuteAfter())
                {
                    addExecuteAfterRelationship(g, provider, errors, clz);
                }
            }

            // add connections to ruleproviders that should execute after this one
            for (Class<? extends RuleProvider> clz : provider.getMetadata().getExecuteBefore())
            {
                addExecuteBeforeRelationship(g, provider, errors, clz);
            }

            if (phaseProvider != null)
            {
                for (Class<? extends RuleProvider> clz : phaseProvider.getMetadata().getExecuteBefore())
                {
                    addExecuteBeforeRelationship(g, provider, errors, clz);
                }
            }

            // add connections to ruleproviders that should execute before this one (by String ID)
            for (String depID : provider.getMetadata().getExecuteAfterIDs())
            {
                RuleProvider otherProvider = getByID(depID);
                if (otherProvider == null)
                {
                    errors.add("RuleProvider " + provider.getMetadata().getID() + " is specified to execute after: "
                                + depID + " but this provider could not be found.");
                }
                else
                    g.addEdge(otherProvider, provider);
            }

            // add connections to ruleproviders that should execute before this one (by String ID)
            for (String depID : provider.getMetadata().getExecuteBeforeIDs())
            {
                RuleProvider otherProvider = getByID(depID);
                if (otherProvider == null)
                {
                    errors.add("RuleProvider " + provider.getMetadata().getID() + " is specified to execute before: "
                                + depID + " but this provider could not be found.");
                }
                else
                    g.addEdge(provider, otherProvider);
            }

            // Report the errors.
            if (!errors.isEmpty())
                throw new WindupMultiStringException("Some rules to be executed before or after were not found:", errors);

        }
    }

    @SuppressWarnings("unchecked")
    private void linkRulePhases()
    {
        // Go through all of the RulePhases and link tail-to-head-to-tail
        // Basically this just makes sure that each phase has both an executeBefore and an executeAfter, linked
        // to the next phase in the execution cycle. This isn't strictly necessary, but it does make debugging easier,
        // and makes the execution order more predictable.
        for (RuleProvider provider : providers)
        {
            if (provider instanceof RulePhase)
            {
                if (provider.getMetadata().getExecuteBefore().isEmpty())
                {
                    // find any that should execute after this one
                    for (RuleProvider otherProvider : providers)
                    {
                        if (otherProvider instanceof RulePhase)
                        {
                            if (otherProvider.getMetadata().getExecuteAfter().contains(Proxies.unwrap(provider).getClass()))
                            {
                                ((RulePhase) provider).setExecuteBefore((Class<? extends RulePhase>) Proxies.unwrap((RulePhase) otherProvider)
                                            .getClass());
                            }
                        }
                    }
                }
                if (provider.getMetadata().getExecuteAfter().isEmpty())
                {
                    // find any that should execute before this one
                    for (RuleProvider otherProvider : providers)
                    {
                        if (otherProvider instanceof RulePhase)
                        {
                            if (otherProvider.getMetadata().getExecuteBefore().contains(Proxies.unwrap(provider).getClass()))
                            {
                                ((RulePhase) provider).setExecuteAfter((Class<? extends RulePhase>) Proxies.unwrap((RulePhase) otherProvider)
                                            .getClass());
                            }
                        }
                    }
                }
            }
        }
    }

    private void addExecuteBeforeRelationship(DefaultDirectedWeightedGraph<RuleProvider, DefaultEdge> g, RuleProvider provider,
                List<String> errors, Class<? extends RuleProvider> clz)
    {
        RuleProvider otherProvider = getByClass(clz);
        if (otherProvider == null)
        {
            errors.add("RuleProvider " + provider.getMetadata().getID() + " is specified to execute before: "
                        + clz.getName() + " but this class could not be found.");
        }
        else
            g.addEdge(provider, otherProvider);
    }

    private void addExecuteAfterRelationship(DefaultDirectedWeightedGraph<RuleProvider, DefaultEdge> g, RuleProvider provider,
                List<String> errors, Class<? extends RuleProvider> clz)
    {
        RuleProvider otherProvider = getByClass(clz);
        if (otherProvider == null)
        {
            errors.add("RuleProvider " + provider.getMetadata().getID() + " is specified to execute after class: "
                        + clz.getName() + " but this class could not be found.");
        }
        else
            g.addEdge(otherProvider, provider);
    }

    /**
     * Use the jgrapht cycle checker to detect any cycles in the provided dependency graph.
     */
    private void checkForCycles(DefaultDirectedWeightedGraph<RuleProvider, DefaultEdge> g)
    {
        CycleDetector<RuleProvider, DefaultEdge> cycleDetector = new CycleDetector<>(g);

        if (cycleDetector.detectCycles())
        {
            // if we have cycles, then try to throw an exception with some usable data
            Set<RuleProvider> cycles = cycleDetector.findCycles();
            StringBuilder errorSB = new StringBuilder();
            for (RuleProvider cycle : cycles)
            {
                errorSB.append("Found dependency cycle involving: " + cycle.getMetadata().getID() + "\n");
                Set<RuleProvider> subCycleSet = cycleDetector.findCyclesContainingVertex(cycle);
                for (RuleProvider subCycle : subCycleSet)
                {
                    errorSB.append("\tSubcycle: " + subCycle.getMetadata().getID() + "\n");
                }
            }
            throw new RuntimeException("Dependency cycles detected: " + errorSB.toString());
        }
    }

    private RuleProvider getByClass(Class<? extends RuleProvider> c)
    {
        return classToProviderMap.get(c);
    }

    private RuleProvider getByID(String id)
    {
        return idToProviderMap.get(id);
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> unwrapType(Class<T> wrapped)
    {
        return (Class<T>) Proxies.unwrapProxyTypes(wrapped);
    }
}
