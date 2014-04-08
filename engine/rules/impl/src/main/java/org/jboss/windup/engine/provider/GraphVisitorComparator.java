package org.jboss.windup.engine.provider;

import java.util.Comparator;
import java.util.List;

import org.jboss.forge.furnace.proxy.Proxies;
import org.jboss.windup.engine.visitor.GraphVisitor;

public class GraphVisitorComparator implements Comparator<GraphVisitor>
{
    @Override
    public int compare(GraphVisitor o1, GraphVisitor o2)
    {
        if (o1.getPhase().getPriority() == o2.getPhase().getPriority()) {
            List<Class<? extends GraphVisitor>> o1Deps = o1.getDependencies();
            List<Class<? extends GraphVisitor>> o2Deps = o2.getDependencies();

            // shortcut (don't check contains either way if both lists are empty)
            if (o1Deps.isEmpty() && o2Deps.isEmpty()) {
                return 0;
            } else {
                Class<?> o1Class = Proxies.unwrapProxyTypes(o1.getClass());
                Class<?> o2Class = Proxies.unwrapProxyTypes(o2.getClass());

                if (o2Deps.contains(o1Class)) {
                    return -1;
                } else if (o1Deps.contains(o2Class)) {
                    return 1;
                } else {
                    return 0;
                }
            }
        } else {
            return o1.getPhase().getPriority() - o2.getPhase().getPriority();
        }
    }
}
