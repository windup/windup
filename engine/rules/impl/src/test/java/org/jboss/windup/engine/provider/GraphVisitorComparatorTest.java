package org.jboss.windup.engine.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.GraphVisitor;
import org.jboss.windup.engine.visitor.VisitorPhase;
import org.junit.Assert;
import org.junit.Test;

public class GraphVisitorComparatorTest
{

    private class VisitorPhase1Class1 extends AbstractGraphVisitor {
        @Override
        public VisitorPhase getPhase()
        {
            return VisitorPhase.DISCOVERY;
        }
        @Override
        public void run()
        {
        }
    }
    
    private class VisitorPhase1Class2 extends AbstractGraphVisitor {
        @Override
        public List<Class<? extends GraphVisitor>> getDependencies()
        {
            List<Class<? extends GraphVisitor>> l = new ArrayList<>();
            l.add(VisitorPhase1Class1.class);
            return l;
        }
        @Override
        public VisitorPhase getPhase()
        {
            return VisitorPhase.DISCOVERY;
        }
        @Override
        public void run()
        {
        }
    }
    
    private class VisitorPhase2Class1 extends AbstractGraphVisitor {
        @Override
        public VisitorPhase getPhase()
        {
            return VisitorPhase.INITIAL_ANALYSIS;
        }
        @Override
        public void run()
        {
        }
    }
    
    @Test
    public void testAll3()
    {
        GraphVisitor v1 = new VisitorPhase1Class1();
        GraphVisitor v2 = new VisitorPhase1Class2();
        GraphVisitor v3 = new VisitorPhase2Class1();
        List<GraphVisitor> visitors = new ArrayList<GraphVisitor>();
        visitors.add(v3);
        visitors.add(v2);
        visitors.add(v1);
        
        Collections.sort(visitors, new GraphVisitorComparator());
        
        Assert.assertEquals(v1, visitors.get(0));
        Assert.assertEquals(v2, visitors.get(1));
        Assert.assertEquals(v3, visitors.get(2));
    }

}
