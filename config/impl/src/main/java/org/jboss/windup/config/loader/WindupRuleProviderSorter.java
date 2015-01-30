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
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.util.exception.WindupMultiStringException;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

/**
 * Sorts {@link WindupRuleProvider}s based upon their executeBefore/executeAfter methods.
 *
 * @author jsightler <jesse.sightler@gmail.com>
 * @author Ondrej Zizka <zizka@seznam.com>
 */
public class WindupRuleProviderSorter
{
    /**
     * All {@link WindupRuleProvider}s
     */
    private List<WindupRuleProvider> providers;

    /**
     * Maps from the WindupRuleProvider class back to the instance of WindupRuleProvider
     */
    private final IdentityHashMap<Class<? extends WindupRuleProvider>, WindupRuleProvider> classToProviderMap = new IdentityHashMap<>();

    /**
     * Maps from the provider's ID to the RuleProvider
     */
    private final Map<String, WindupRuleProvider> idToProviderMap = new HashMap<>();

    private WindupRuleProviderSorter(List<WindupRuleProvider> providers)
    {
        this.providers = new ArrayList<>(providers);
        initializeLookupCaches();
        sort();
    }

    /**
     * Sort the provided list of {@link WindupRuleProvider}s and return the result.
     */
    public static List<WindupRuleProvider> sort(List<WindupRuleProvider> providers)
    {
        WindupRuleProviderSorter sorter = new WindupRuleProviderSorter(providers);
        return sorter.getProviders();
    }

    /**
     * Gets the provider list
     */
    private List<WindupRuleProvider> getProviders()
    {
        return providers;
    }

    /**
     * Initializes lookup caches that are used during sort to lookup providers by ID or Java {@link Class}.
     */
    private void initializeLookupCaches()
    {
        // Initialize lookup maps
        for (WindupRuleProvider provider : providers)
        {
            Class<? extends WindupRuleProvider> unproxiedClass = unwrapType(provider.getClass());
            classToProviderMap.put(unproxiedClass, provider);
            idToProviderMap.put(provider.getID(), provider);
        }
    }

    /**
     * Perform the entire sort operation
     */
    private void sort()
    {
        // Build a directed graph based upon the dependencies
        DefaultDirectedWeightedGraph<WindupRuleProvider, DefaultEdge> g = new DefaultDirectedWeightedGraph<>(
                    DefaultEdge.class);

        // Add initial vertices to the graph
        // Initialize lookup maps
        for (WindupRuleProvider provider : providers)
        {
            g.addVertex(provider);
        }

        addProviderRelationships(g);

        checkForCycles(g);

        // create the final results list
        List<WindupRuleProvider> result = new ArrayList<WindupRuleProvider>(this.providers.size());
        // use topological ordering to make it all the right order
        TopologicalOrderIterator<WindupRuleProvider, DefaultEdge> iterator = new TopologicalOrderIterator<>(g);
        while (iterator.hasNext())
        {
            WindupRuleProvider provider = iterator.next();
            result.add(provider);
        }

        this.providers = Collections.unmodifiableList(result);

        int index = 0;
        for (WindupRuleProvider provider : this.providers)
        {
            provider.setExecutionIndex(index++);
        }
    }

    /**
     * Add edges between {@link WinduPRuleProvider}s based upon their dependency relationships.
     */
    private void addProviderRelationships(DefaultDirectedWeightedGraph<WindupRuleProvider, DefaultEdge> g)
    {
        linkRulePhases();

        for (WindupRuleProvider provider : providers)
        {
            WindupRuleProvider phaseProvider = getByClass(provider.getPhase());

            List<String> errors = new LinkedList<>();

            // add connections to ruleproviders that should execute before this one
            for (Class<? extends WindupRuleProvider> clz : provider.getExecuteAfter())
            {
                addExecuteAfterRelationship(g, provider, errors, clz);
            }

            if (phaseProvider != null)
            {
                if (provider.getPhase() != Proxies.unwrap(provider).getClass())
                    addExecuteAfterRelationship(g, provider, errors, provider.getPhase());

                for (Class<? extends WindupRuleProvider> clz : phaseProvider.getExecuteAfter())
                {
                    addExecuteAfterRelationship(g, provider, errors, clz);
                }
            }

            // add connections to ruleproviders that should execute after this one
            for (Class<? extends WindupRuleProvider> clz : provider.getExecuteBefore())
            {
                addExecuteBeforeRelationship(g, provider, errors, clz);
            }

            if (phaseProvider != null)
            {
                for (Class<? extends WindupRuleProvider> clz : phaseProvider.getExecuteBefore())
                {
                    addExecuteBeforeRelationship(g, provider, errors, clz);
                }
            }

            // add connections to ruleproviders that should execute before this one (by String ID)
            for (String depID : provider.getExecuteAfterIDs())
            {
                WindupRuleProvider otherProvider = getByID(depID);
                if (otherProvider == null)
                {
                    errors.add("RuleProvider " + provider.getID() + " is specified to execute after: "
                                + depID + " but this provider could not be found.");
                }
                else
                    g.addEdge(otherProvider, provider);
            }

            // add connections to ruleproviders that should execute before this one (by String ID)
            for (String depID : provider.getExecuteBeforeIDs())
            {
                WindupRuleProvider otherProvider = getByID(depID);
                if (otherProvider == null)
                {
                    errors.add("RuleProvider " + provider.getID() + " is specified to execute before: "
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
        for (WindupRuleProvider provider : providers)
        {
            if (provider instanceof RulePhase)
            {
                if (provider.getExecuteBefore().isEmpty())
                {
                    // find any that should execute after this one
                    for (WindupRuleProvider otherProvider : providers)
                    {
                        if (otherProvider instanceof RulePhase)
                        {
                            if (otherProvider.getExecuteAfter().contains(Proxies.unwrap(provider).getClass()))
                            {
                                ((RulePhase) provider).setExecuteBefore((Class<? extends RulePhase>) Proxies.unwrap((RulePhase) otherProvider)
                                            .getClass());
                            }
                        }
                    }
                }
                if (provider.getExecuteAfter().isEmpty())
                {
                    // find any that should execute before this one
                    for (WindupRuleProvider otherProvider : providers)
                    {
                        if (otherProvider instanceof RulePhase)
                        {
                            if (otherProvider.getExecuteBefore().contains(Proxies.unwrap(provider).getClass()))
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

    private void addExecuteBeforeRelationship(DefaultDirectedWeightedGraph<WindupRuleProvider, DefaultEdge> g, WindupRuleProvider provider,
                List<String> errors, Class<? extends WindupRuleProvider> clz)
    {
        WindupRuleProvider otherProvider = getByClass(clz);
        if (otherProvider == null)
        {
            errors.add("RuleProvider " + provider.getID() + " is specified to execute before: "
                        + clz.getName() + " but this class could not be found.");
        }
        else
            g.addEdge(provider, otherProvider);
    }

    private void addExecuteAfterRelationship(DefaultDirectedWeightedGraph<WindupRuleProvider, DefaultEdge> g, WindupRuleProvider provider,
                List<String> errors, Class<? extends WindupRuleProvider> clz)
    {
        WindupRuleProvider otherProvider = getByClass(clz);
        if (otherProvider == null)
        {
            errors.add("RuleProvider " + provider.getID() + " is specified to execute after class: "
                        + clz.getName() + " but this class could not be found.");
        }
        else
            g.addEdge(otherProvider, provider);
    }

    /**
     * Use the jgrapht cycle checker to detect any cycles in the provided dependency graph.
     */
    private void checkForCycles(DefaultDirectedWeightedGraph<WindupRuleProvider, DefaultEdge> g)
    {
        CycleDetector<WindupRuleProvider, DefaultEdge> cycleDetector = new CycleDetector<>(g);

        if (cycleDetector.detectCycles())
        {
            // if we have cycles, then try to throw an exception with some usable data
            Set<WindupRuleProvider> cycles = cycleDetector.findCycles();
            StringBuilder errorSB = new StringBuilder();
            for (WindupRuleProvider cycle : cycles)
            {
                errorSB.append("Found dependency cycle involving: " + cycle.getID() + "\n");
                Set<WindupRuleProvider> subCycleSet = cycleDetector.findCyclesContainingVertex(cycle);
                for (WindupRuleProvider subCycle : subCycleSet)
                {
                    errorSB.append("\tSubcycle: " + subCycle.getID() + "\n");
                }
            }
            throw new RuntimeException("Dependency cycles detected: " + errorSB.toString());
        }
    }

    private WindupRuleProvider getByClass(Class<? extends WindupRuleProvider> c)
    {
        return classToProviderMap.get(c);
    }

    private WindupRuleProvider getByID(String id)
    {
        return idToProviderMap.get(id);
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> unwrapType(Class<T> wrapped)
    {
        return (Class<T>) Proxies.unwrapProxyTypes(wrapped);
    }
}
