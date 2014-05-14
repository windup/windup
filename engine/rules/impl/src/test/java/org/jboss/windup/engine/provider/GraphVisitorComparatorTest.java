package org.jboss.windup.engine.provider;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.addon.config.RulePhase;
import org.jboss.windup.engine.visitor.AbstractGraphVisitor;
import org.jboss.windup.engine.visitor.GraphVisitor;
import org.junit.Assert;
import org.junit.Test;

public class GraphVisitorComparatorTest
{

    private class VisitorPhase1Class1 extends AbstractGraphVisitor {
        private List<Class<? extends GraphVisitor>> deps = new ArrayList<>();
        
        @Override
        public List<Class<? extends GraphVisitor>> getDependencies()
        {
            return deps;
        }
        @Override
        public RulePhase getPhase()
        {
            return RulePhase.DISCOVERY;
        }
        @Override
        public void run()
        {
        }
        @Override
        public String toString()
        {
            return "Phase1Class1";
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
        public RulePhase getPhase()
        {
            return RulePhase.DISCOVERY;
        }
        @Override
        public void run()
        {
        }
        @Override
        public String toString()
        {
            return "Phase1Class2";
        }
    }
    
    private class VisitorPhase1Class3 extends AbstractGraphVisitor {
        @Override
        public List<Class<? extends GraphVisitor>> getDependencies()
        {
            List<Class<? extends GraphVisitor>> l = new ArrayList<>();
            l.add(VisitorPhase1Class2.class);
            return l;
        }
        @Override
        public RulePhase getPhase()
        {
            return RulePhase.DISCOVERY;
        }
        @Override
        public void run()
        {
        }
        @Override
        public String toString()
        {
            return "Phase1Class3";
        }
    }
    
    private class VisitorPhase2Class1 extends AbstractGraphVisitor {
        @Override
        public RulePhase getPhase()
        {
            return RulePhase.INITIAL_ANALYSIS;
        }
        @Override
        public void run()
        {
        }
        @Override
        public String toString()
        {
            return "Phase2Class1";
        }
    }
    
    @Test
    public void testSort()
    {
        GraphVisitor v1 = new VisitorPhase1Class1();
        GraphVisitor v2 = new VisitorPhase1Class2();
        GraphVisitor v3 = new VisitorPhase1Class3();
        GraphVisitor v4 = new VisitorPhase2Class1();
        List<GraphVisitor> visitors = new ArrayList<GraphVisitor>();
        visitors.add(v3);
        visitors.add(v4);
        visitors.add(v2);
        visitors.add(v1);
        
        GraphVisitorSorter sorter = new GraphVisitorSorter();
        List<GraphVisitor> sortedVisitors = sorter.sort(visitors);
        
        System.out.println("Results: " + sortedVisitors);
        
        Assert.assertEquals(v1, sortedVisitors.get(0));
        Assert.assertEquals(v2, sortedVisitors.get(1));
        Assert.assertEquals(v3, sortedVisitors.get(2));
        Assert.assertEquals(v4, sortedVisitors.get(3));
    }
    
    @Test
    public void testSortCycle()
    {
        VisitorPhase1Class1 v1 = new VisitorPhase1Class1();
        v1.deps.add(VisitorPhase1Class3.class);
        GraphVisitor v2 = new VisitorPhase1Class2();
        GraphVisitor v3 = new VisitorPhase1Class3();
        GraphVisitor v4 = new VisitorPhase2Class1();
        List<GraphVisitor> visitors = new ArrayList<GraphVisitor>();
        visitors.add(v3);
        visitors.add(v4);
        visitors.add(v2);
        visitors.add(v1);
        
        GraphVisitorSorter sorter = new GraphVisitorSorter();
        try {
            List<GraphVisitor> sortedVisitors = sorter.sort(visitors);
            Assert.fail("No cycles detected");
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Dependency cycles detected"));
        }
    }

}
