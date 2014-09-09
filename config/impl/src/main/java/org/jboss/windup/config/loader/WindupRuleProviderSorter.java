package org.jboss.windup.config.loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.forge.furnace.proxy.Proxies;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.util.exception.WindupException;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

/**
 * Sorts {@link WindupRuleProvider}s based upon their Phase and executeBefore/executeAfter methods.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
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
    private IdentityHashMap<Class<? extends WindupRuleProvider>, WindupRuleProvider> classToProviderMap = new IdentityHashMap<>();

    /**
     * Maps from the provider's ID to the RuleProvider
     */
    private Map<String, WindupRuleProvider> idToProviderMap = new HashMap<>();

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

        sortByPhase();

        // check for phase relationships that would cause cycles
        checkForImproperPhaseRelationships();

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
    }

    /**
     * Sort the providers by phase
     */
    private void sortByPhase()
    {
        Collections.sort(providers, new Comparator<WindupRuleProvider>()
        {
            @Override
            public int compare(WindupRuleProvider o1, WindupRuleProvider o2)
            {
                return o1.getPhase().getPriority() - o2.getPhase().getPriority();
            }
        });
    }

    /**
     * Add edges between {@link WinduPRuleProvider}s based upon their dependency relationships.
     */
    private void addProviderRelationships(DefaultDirectedWeightedGraph<WindupRuleProvider, DefaultEdge> g)
    {
        // Keep a list of all visitors from the previous phase
        // This allows us to create edges from nodes in one phase to the next,
        // allowing the topological sort to sort by phases as well.
        List<WindupRuleProvider> previousProviders = new ArrayList<>();
        List<WindupRuleProvider> currentProviders = new ArrayList<>();
        RulePhase previousPhase = null;
        for (WindupRuleProvider provider : providers)
        {
            RulePhase currentPhase = provider.getPhase();

            if (currentPhase != previousPhase && currentPhase != RulePhase.IMPLICIT)
            {
                // we've reached a new phase, so move the current phase to the last
                previousProviders.clear();
                previousProviders.addAll(currentProviders);
                currentProviders.clear();
            }
            currentProviders.add(provider);

            // add connections to ruleproviders that should execute before this one
            for (Class<? extends WindupRuleProvider> clz : provider.getExecuteAfter())
            {
                WindupRuleProvider otherProvider = getByClass(clz);
                if (otherProvider == null)
                {
                    throw new WindupException("Configuration Provider: " + provider.getID()
                                + " is specified to execute after class: "
                                + clz.getCanonicalName() + " but this class could not be found!");
                }
                g.addEdge(otherProvider, provider);
            }

            // add connections to ruleproviders that should execute after this one
            for (Class<? extends WindupRuleProvider> clz : provider.getExecuteBefore())
            {
                WindupRuleProvider otherProvider = getByClass(clz);
                if (otherProvider == null)
                {
                    throw new WindupException("Configuration Provider: " + provider.getID()
                                + " is specified to execute before: "
                                + clz.getCanonicalName() + " but this class could not be found!");
                }
                g.addEdge(provider, otherProvider);
            }

            // add connections to ruleproviders that should execute before this one (by String ID)
            for (String depID : provider.getExecuteAfterIDs())
            {
                WindupRuleProvider otherProvider = getByID(depID);
                if (otherProvider == null)
                {
                    throw new WindupException("Configuration Provider: " + provider.getID()
                                + " is specified to execute after: "
                                + depID + " but this provider could not be found!");
                }
                g.addEdge(otherProvider, provider);
            }

            // add connections to ruleproviders that should execute before this one (by String ID)
            for (String depID : provider.getExecuteBeforeIDs())
            {
                WindupRuleProvider otherProvider = getByID(depID);
                if (otherProvider == null)
                {
                    throw new WindupException("Configuration Provider: " + provider.getID()
                                + " is specified to execute before: "
                                + depID + " but this provider could not be found!");
                }
                g.addEdge(provider, otherProvider);
            }

            // also, if the current provider is not an implicit phase, then
            // add dependencies onto all visitors from the previous phase
            if (currentPhase != RulePhase.IMPLICIT)
            {
                for (WindupRuleProvider prevV : previousProviders)
                {
                    g.addEdge(prevV, provider);
                }
            }
            previousPhase = currentPhase;
        }
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

    /**
     * Check that no rules from earlier phases have inadvertantly become dependent upon rules from later phases.
     */
    private void checkForImproperPhaseRelationships()
    {
        for (WindupRuleProvider provider : this.providers)
        {
            RulePhase rulePhase = provider.getPhase();
            if (rulePhase == null)
            {
                // just make sure it has at least one dependency
                if ((provider.getExecuteAfter() == null || provider.getExecuteAfter().isEmpty()) &&
                            (provider.getExecuteAfterIDs() == null || provider.getExecuteAfterIDs().isEmpty()) &&
                            (provider.getExecuteBefore() == null || provider.getExecuteBefore().isEmpty()) &&
                            (provider.getExecuteBeforeIDs() == null || provider.getExecuteBeforeIDs().isEmpty()))
                {
                    throw new IncorrectPhaseDependencyException("Error, rule \"" + provider.getID()
                                + "\" Uses an implicit phase (phase is null) "
                                + " but does not specify any dependencies");
                }

                continue;
            }

            for (Class<? extends WindupRuleProvider> classDep : provider.getExecuteAfter())
            {
                WindupRuleProvider otherProvider = getByClass(classDep);
                if (otherProvider == null)
                {
                    // skip, as we will check for these types of errors
                    continue;
                }
                if (!phaseRelationshipOk(otherProvider, provider))
                {
                    throw new IncorrectPhaseDependencyException("Error, rule \"" + provider.getID()
                                + "\" from phase \"" + rulePhase
                                + "\" is to set to execute after rule \"" + otherProvider.getID() + "\""
                                + " from phase \""
                                + otherProvider.getPhase()
                                + "\". Rules must only specify rules " + rulePhase + " or an earlier phase.");
                }
            }

            for (Class<? extends WindupRuleProvider> classDep : provider.getExecuteBefore())
            {
                WindupRuleProvider otherProvider = getByClass(classDep);
                if (otherProvider == null)
                {
                    // skip, as we will check for these types of conditions later
                    continue;
                }
                if (!phaseRelationshipOk(provider, otherProvider))
                {
                    throw new IncorrectPhaseDependencyException("Error, rule \"" + provider.getID()
                                + "\" from phase \"" + rulePhase
                                + "\" is to set to execute before rule \"" + otherProvider.getID() + "\""
                                + " from phase \""
                                + otherProvider.getPhase()
                                + "\". Rules must only specify rules " + rulePhase + " or a later phase.");
                }
            }

            for (String idDep : provider.getExecuteAfterIDs())
            {
                WindupRuleProvider otherProvider = getByID(idDep);
                if (otherProvider == null)
                {
                    // skip, as we will check for these types of conditions later
                    continue;
                }
                if (!phaseRelationshipOk(otherProvider, provider))
                {
                    throw new IncorrectPhaseDependencyException("Error, rule \"" + provider.getID()
                                + "\" from phase \"" + rulePhase
                                + "\" is to set to execute after rule \"" + otherProvider.getID() + "\""
                                + " from phase \""
                                + otherProvider.getPhase()
                                + "\". Rules must only specify rules " + rulePhase + " or an earlier phase.");
                }
            }

            for (String idDep : provider.getExecuteBeforeIDs())
            {
                WindupRuleProvider otherProvider = getByID(idDep);
                if (otherProvider == null)
                {
                    // skip, as we will check for these types of conditions later
                    continue;
                }
                if (!phaseRelationshipOk(provider, otherProvider))
                {
                    throw new IncorrectPhaseDependencyException("Error, rule \"" + provider.getID()
                                + "\" from phase \"" + rulePhase
                                + "\" is to set to execute before rule \"" + otherProvider.getID() + "\""
                                + " from phase \""
                                + otherProvider.getPhase()
                                + "\". Rules must only specify rules " + rulePhase + " or a later phase.");
                }
            }
        }
    }

    private static boolean phaseRelationshipOk(WindupRuleProvider before, WindupRuleProvider after)
    {
        RulePhase beforePhase = before.getPhase();
        RulePhase afterPhase = after.getPhase();
        if (beforePhase == RulePhase.IMPLICIT || afterPhase == RulePhase.IMPLICIT)
        {
            return true;
        }
        return beforePhase.getPriority() <= afterPhase.getPriority();
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
