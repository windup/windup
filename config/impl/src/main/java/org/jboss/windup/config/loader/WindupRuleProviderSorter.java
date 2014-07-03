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

public class WindupRuleProviderSorter
{
    public static List<WindupRuleProvider> sort(
                List<WindupRuleProvider> windupRuleProviderList)
    {
        // add all items to a temporary list (to avoid making gratuitous modifications to the original list)
        List<WindupRuleProvider> tempList = new ArrayList<WindupRuleProvider>(
                    windupRuleProviderList);

        // Sort by phase
        Collections.sort(tempList, new Comparator<WindupRuleProvider>()
        {
            @Override
            public int compare(WindupRuleProvider o1, WindupRuleProvider o2)
            {
                return o1.getPhase().getPriority() - o2.getPhase().getPriority();
            }
        });

        // Create a map to get back from Class to Object
        // (this helps as we will sort the dependencies by class, but we want to ultimately return a list of
        // GraphVisitor Objects)
        IdentityHashMap<WindupRuleProvider, WindupRuleProvider> unwrappedToWrappedMap = new IdentityHashMap<>();

        IdentityHashMap<Class<? extends WindupRuleProvider>, WindupRuleProvider> classToCfgProviderMap = new IdentityHashMap<>();
        Map<String, WindupRuleProvider> idToCfgProviderMap = new HashMap<>();

        // Now build a directed graph based upon the dependencies
        DefaultDirectedWeightedGraph<WindupRuleProvider, DefaultEdge> g = new DefaultDirectedWeightedGraph<>(
                    DefaultEdge.class);
        // Also, keep this around to make sure we didn't accidentally introduce any cyclic dependencies
        CycleDetector<WindupRuleProvider, DefaultEdge> cycleDetector = new CycleDetector<>(g);

        // Add the initial vertices and the class to object mapping
        for (WindupRuleProvider v : tempList)
        {
            @SuppressWarnings("unchecked")
            Class<? extends WindupRuleProvider> unproxiedClass = (Class<? extends WindupRuleProvider>) Proxies
                        .unwrapProxyTypes(v
                                    .getClass());

            WindupRuleProvider unwrappedObject = unwrap(v);
            unwrappedToWrappedMap.put(unwrappedObject, v);
            classToCfgProviderMap.put(unproxiedClass, v);
            idToCfgProviderMap.put(v.getID(), v);
            g.addVertex(unwrappedObject);
        }

        checkForImproperPhaseDependencies(classToCfgProviderMap, tempList);

        // Keep a list of all visitors from the previous phase
        // This allows us to create edges from nodes in one phase to the next,
        // allowing the topological sort to sort by phases as well.
        List<WindupRuleProvider> previousCfgProviders = new ArrayList<>();
        List<WindupRuleProvider> currentCfgProviders = new ArrayList<>();
        RulePhase previousPhase = null;
        for (WindupRuleProvider v : tempList)
        {
            if (v.getPhase() != previousPhase)
            {
                // we've reached a new phase, so move the current phase to the last
                previousCfgProviders.clear();
                previousCfgProviders.addAll(currentCfgProviders);
                currentCfgProviders.clear();
            }
            currentCfgProviders.add(v);

            // add dependencies for each visitor by class
            for (Class<? extends WindupRuleProvider> clz : v.getClassDependencies())
            {
                WindupRuleProvider otherProvider = classToCfgProviderMap.get(clz);
                if (otherProvider == null)
                {
                    throw new WindupException("Configuration Provider: " + v.getID() + " depends on class: "
                                + clz.getCanonicalName() + " but this class could not be found!");
                }
                g.addEdge(unwrap(otherProvider), unwrap(v));
            }

            // add dependencies for each visitor by id
            for (String depID : v.getIDDependencies())
            {
                WindupRuleProvider otherProvider = idToCfgProviderMap.get(depID);
                if (otherProvider == null)
                {
                    throw new WindupException("Configuration Provider: " + v.getID()
                                + " depends on configuration provider: "
                                + depID + " but this provider could not be found!");
                }
                g.addEdge(unwrap(otherProvider), unwrap(v));
            }

            // also, add dependencies onto all visitors from the previous phase
            for (WindupRuleProvider prevV : previousCfgProviders)
            {
                g.addEdge(unwrap(prevV), unwrap(v));
            }
            previousPhase = v.getPhase();
        }

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

        // create the final results list
        List<WindupRuleProvider> result = new ArrayList<WindupRuleProvider>(tempList.size());
        // use topological ordering to make it all the right order
        TopologicalOrderIterator<WindupRuleProvider, DefaultEdge> iterator = new TopologicalOrderIterator<>(
                    g);
        while (iterator.hasNext())
        {
            WindupRuleProvider provider = iterator.next();
            result.add(unwrappedToWrappedMap.get(provider));
        }

        return result;

    }

    private static void checkForImproperPhaseDependencies(
                IdentityHashMap<Class<? extends WindupRuleProvider>, WindupRuleProvider> classToCfgProviderMap,
                List<WindupRuleProvider> ruleProviders)
    {
        for (WindupRuleProvider ruleProvider : ruleProviders)
        {
            RulePhase rulePhase = ruleProvider.getPhase();

            for (Class<? extends WindupRuleProvider> classDep : ruleProvider.getClassDependencies())
            {
                WindupRuleProvider otherRuleProvider = classToCfgProviderMap.get(classDep);
                if (rulePhase != otherRuleProvider.getPhase())
                {
                    throw new IncorrectPhaseDependencyException("Error, rule \"" + ruleProvider.getID()
                                + "\" from phase \"" + rulePhase
                                + "\" depends on rule \"" + otherRuleProvider.getID() + "\"" + " from phase \""
                                + otherRuleProvider.getPhase()
                                + "\". Rules must only depend on other rules from within the same phase.");
                }
            }
        }
    }

    private static WindupRuleProvider unwrap(WindupRuleProvider provider)
    {
        return Proxies.unwrap(provider);
    }
}
