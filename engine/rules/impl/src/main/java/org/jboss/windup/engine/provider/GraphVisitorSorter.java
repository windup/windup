package org.jboss.windup.engine.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import org.jboss.forge.furnace.proxy.Proxies;
import org.jboss.windup.engine.visitor.GraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

public class GraphVisitorSorter
{

    /**
     * Returns a list of visitors, sorted by the phase and dependency. Note that this
     * does _not_ modify the originally passed in list.
     * 
     * @param graphVisitorList
     * @return
     */
    public List<GraphVisitor> sort(List<GraphVisitor> graphVisitorList) {
        // add all items to a temporary list (to avoid making gratuitous modifications to the original list)
        List<GraphVisitor> tempList = new ArrayList<GraphVisitor>(graphVisitorList.size());
        tempList.addAll(graphVisitorList);
        
        // Sort by phase
        Collections.sort(tempList, new Comparator<GraphVisitor>()
        {
            @Override
            public int compare(GraphVisitor o1, GraphVisitor o2)
            {
                return o1.getPhase().getPriority() - o2.getPhase().getPriority();
            }
        });
        
        // Create a map to get back from Class to Object
        // (this helps as we will sort the dependencies by class, but we want to ultimately return a list of GraphVisitor Objects)
        IdentityHashMap<Class<? extends GraphVisitor>, GraphVisitor> classToVisitorMap = new IdentityHashMap<>();
        
        // Now build a directed graph based upon the dependencies
        DefaultDirectedWeightedGraph<Class<? extends GraphVisitor>, DefaultEdge> g = new DefaultDirectedWeightedGraph<>(DefaultEdge.class);
        // Also, keep this around to make sure we didn't accidentally introduce any cyclic dependencies
        CycleDetector<Class<? extends GraphVisitor>, DefaultEdge> cycleDetector = new CycleDetector<>(g);

        // Add the initial vertices and the class to object mapping
        for (GraphVisitor v : tempList) {
            @SuppressWarnings("unchecked")
            Class<? extends GraphVisitor> unproxiedClass = (Class<? extends GraphVisitor>)Proxies.unwrapProxyTypes(v.getClass());
            
            classToVisitorMap.put(unproxiedClass, v);
            g.addVertex(unproxiedClass);
        }
        
        // Keep a list of all visitors from the previous phase
        // This allows us to create edges from nodes in one phase to the next,
        // allowing the topological sort to sort by phases as well.
        List<GraphVisitor> previousPhaseVisitors = new ArrayList<>();
        List<GraphVisitor> currentPhaseVisitors = new ArrayList<>();
        VisitorPhase previousPhase = null;
        for (GraphVisitor v : tempList) {
            @SuppressWarnings("unchecked")
            Class<? extends GraphVisitor> unproxiedClass = (Class<? extends GraphVisitor>)Proxies.unwrapProxyTypes(v.getClass());
            
            if (v.getPhase() != previousPhase) {
                // we've reached a new phase, so move the current phase to the last
                previousPhaseVisitors.clear();
                previousPhaseVisitors.addAll(currentPhaseVisitors);
                currentPhaseVisitors.clear();
            }
            currentPhaseVisitors.add(v);
            
            // add dependencies for each visitor
            for (Class<? extends GraphVisitor> clz : v.getDependencies()) {
                g.addEdge(clz, unproxiedClass);
            }
            
            // also, add dependencies onto all visitors from the previous phase
            for (GraphVisitor prevV : previousPhaseVisitors) {
                @SuppressWarnings("unchecked")
                Class<? extends GraphVisitor> unproxiedPreviousClass = (Class<? extends GraphVisitor>)Proxies.unwrapProxyTypes(prevV.getClass());
                
                g.addEdge(unproxiedPreviousClass, unproxiedClass);
            }
            previousPhase = v.getPhase();
        }
        
        if (cycleDetector.detectCycles()) {
            // if we have cycles, then try to throw an exception with some usable data
            Set<Class<? extends GraphVisitor>> cycles = cycleDetector.findCycles();
            StringBuilder errorSB = new StringBuilder();
            for (Class<? extends GraphVisitor> cycle : cycles) {
                errorSB.append("Found dependency cycle involving: " + cycle + "\n");
                Set<Class<? extends GraphVisitor>> subCycleSet = cycleDetector.findCyclesContainingVertex(cycle);
                for (Class<? extends GraphVisitor> subCycle : subCycleSet) {
                    errorSB.append("\tSubcycle: " + subCycle + "\n");
                }
            }
            throw new RuntimeException("Dependency cycles detected: " + errorSB.toString());
        }
        
        // create the final results list
        List<GraphVisitor> result = new ArrayList<GraphVisitor>(tempList.size());
        // use topological ordering to make it all the right order
        TopologicalOrderIterator<Class<? extends GraphVisitor>, DefaultEdge> iterator = new TopologicalOrderIterator<>(g);
        while (iterator.hasNext()) {
            Class<? extends GraphVisitor> clz = iterator.next();
            result.add(classToVisitorMap.get(clz));
        }
        
        return result;
    }
}
