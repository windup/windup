package org.jboss.windup.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import org.jboss.forge.furnace.proxy.Proxies;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

public class GraphProviderSorter
{
    public static List<WindupConfigurationProvider> sort(
                List<WindupConfigurationProvider> windupConfigurationProviderList)
    {
        // add all items to a temporary list (to avoid making gratuitous modifications to the original list)
        List<WindupConfigurationProvider> tempList = new ArrayList<WindupConfigurationProvider>(
                    windupConfigurationProviderList);

        // Sort by phase
        Collections.sort(tempList, new Comparator<WindupConfigurationProvider>()
        {
            @Override
            public int compare(WindupConfigurationProvider o1, WindupConfigurationProvider o2)
            {
                return o1.getPhase().getPriority() - o2.getPhase().getPriority();
            }
        });

        // Create a map to get back from Class to Object
        // (this helps as we will sort the dependencies by class, but we want to ultimately return a list of
        // GraphVisitor Objects)
        IdentityHashMap<Class<? extends WindupConfigurationProvider>, WindupConfigurationProvider> classToCfgProviderMap = new IdentityHashMap<>();

        // Now build a directed graph based upon the dependencies
        DefaultDirectedWeightedGraph<Class<? extends WindupConfigurationProvider>, DefaultEdge> g = new DefaultDirectedWeightedGraph<>(
                    DefaultEdge.class);
        // Also, keep this around to make sure we didn't accidentally introduce any cyclic dependencies
        CycleDetector<Class<? extends WindupConfigurationProvider>, DefaultEdge> cycleDetector = new CycleDetector<>(g);

        // Add the initial vertices and the class to object mapping
        for (WindupConfigurationProvider v : tempList)
        {
            @SuppressWarnings("unchecked")
            Class<? extends WindupConfigurationProvider> unproxiedClass = (Class<? extends WindupConfigurationProvider>) Proxies
                        .unwrapProxyTypes(v
                                    .getClass());

            classToCfgProviderMap.put(unproxiedClass, v);
            g.addVertex(unproxiedClass);
        }

        // Keep a list of all visitors from the previous phase
        // This allows us to create edges from nodes in one phase to the next,
        // allowing the topological sort to sort by phases as well.
        List<WindupConfigurationProvider> previousCfgProviders = new ArrayList<>();
        List<WindupConfigurationProvider> currentCfgProviders = new ArrayList<>();
        RulePhase previousPhase = null;
        for (WindupConfigurationProvider v : tempList)
        {
            @SuppressWarnings("unchecked")
            Class<? extends WindupConfigurationProvider> unproxiedClass = (Class<? extends WindupConfigurationProvider>) Proxies
                        .unwrapProxyTypes(v
                                    .getClass());

            if (v.getPhase() != previousPhase)
            {
                // we've reached a new phase, so move the current phase to the last
                previousCfgProviders.clear();
                previousCfgProviders.addAll(currentCfgProviders);
                currentCfgProviders.clear();
            }
            currentCfgProviders.add(v);

            // add dependencies for each visitor
            for (Class<? extends WindupConfigurationProvider> clz : v.getDependencies())
            {
                g.addEdge(clz, unproxiedClass);
            }

            // also, add dependencies onto all visitors from the previous phase
            for (WindupConfigurationProvider prevV : previousCfgProviders)
            {
                @SuppressWarnings("unchecked")
                Class<? extends WindupConfigurationProvider> unproxiedPreviousClass = (Class<? extends WindupConfigurationProvider>) Proxies
                            .unwrapProxyTypes(prevV.getClass());

                g.addEdge(unproxiedPreviousClass, unproxiedClass);
            }
            previousPhase = v.getPhase();
        }

        if (cycleDetector.detectCycles())
        {
            // if we have cycles, then try to throw an exception with some usable data
            Set<Class<? extends WindupConfigurationProvider>> cycles = cycleDetector.findCycles();
            StringBuilder errorSB = new StringBuilder();
            for (Class<? extends WindupConfigurationProvider> cycle : cycles)
            {
                errorSB.append("Found dependency cycle involving: " + cycle + "\n");
                Set<Class<? extends WindupConfigurationProvider>> subCycleSet = cycleDetector
                            .findCyclesContainingVertex(cycle);
                for (Class<? extends WindupConfigurationProvider> subCycle : subCycleSet)
                {
                    errorSB.append("\tSubcycle: " + subCycle + "\n");
                }
            }
            throw new RuntimeException("Dependency cycles detected: " + errorSB.toString());
        }

        // create the final results list
        List<WindupConfigurationProvider> result = new ArrayList<WindupConfigurationProvider>(tempList.size());
        // use topological ordering to make it all the right order
        TopologicalOrderIterator<Class<? extends WindupConfigurationProvider>, DefaultEdge> iterator = new TopologicalOrderIterator<>(
                    g);
        while (iterator.hasNext())
        {
            Class<? extends WindupConfigurationProvider> clz = iterator.next();
            result.add(classToCfgProviderMap.get(clz));
        }

        return result;

    }
}
