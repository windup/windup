package org.jboss.windup.engine.provider;

import com.google.common.collect.Lists;
import java.util.List;
import javax.inject.Inject;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.engine.visitor.GraphVisitor;

/**
 * Gets the GraphVisitor instances from Furnace and provides a sorted copy of that list.
 */
public class VisitorChainProvider
{
    @Inject
    private Imported<GraphVisitor> visitors;

    /**
     * Returns a sorted copy of GraphVisitor instances list from Forge.
     */
    public List<GraphVisitor> getSortedVisitorChain()
    {
        List<GraphVisitor> chain = Lists.newArrayList(this.visitors);
        chain = new GraphVisitorSorter().sort(chain);
        return chain;
    }

    public void disposeVisitors(List<GraphVisitor> visitorsToDispose)
    {
        for (GraphVisitor v : visitorsToDispose)
        {
            this.visitors.release(v);
        }
    }
}
