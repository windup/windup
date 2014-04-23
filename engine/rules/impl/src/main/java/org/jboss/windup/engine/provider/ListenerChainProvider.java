package org.jboss.windup.engine.provider;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.engine.visitor.GraphVisitor;
import org.jboss.windup.graph.WindupContext;

public class ListenerChainProvider
{
    @Inject
    private WindupContext context;
    
    @Inject
    private Imported<GraphVisitor> visitors;

    public List<GraphVisitor> getListenerChain()
    {
        List<GraphVisitor> listenerChain = new LinkedList<GraphVisitor>();
        for (GraphVisitor visitor : visitors) {
            listenerChain.add(visitor);
        }
        listenerChain = new GraphVisitorSorter().sort(listenerChain);
        
        return listenerChain;
    }
    
    public void disposeListners(List<GraphVisitor> listeners) {
        for (GraphVisitor l : listeners) {
            visitors.release(l);
        }
    }
}
