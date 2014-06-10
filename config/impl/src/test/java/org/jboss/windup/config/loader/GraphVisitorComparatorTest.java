package org.jboss.windup.config.loader;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupConfigurationProvider;
import org.jboss.windup.graph.GraphContext;
import org.junit.Assert;
import org.junit.Test;
import org.ocpsoft.rewrite.config.Configuration;

public class GraphVisitorComparatorTest
{

    private class VisitorPhase1Class1 extends WindupConfigurationProvider
    {
        private List<Class<? extends WindupConfigurationProvider>> deps = new ArrayList<>();

        @Override
        public List<Class<? extends WindupConfigurationProvider>> getClassDependencies()
        {
            return deps;
        }

        @Override
        public RulePhase getPhase()
        {
            return RulePhase.DISCOVERY;
        }

        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            return null;
        }

        @Override
        public String toString()
        {
            return "Phase1Class1";
        }

        @Override
        public String getID()
        {
            return toString();
        }
    }

    private class VisitorPhase1Class2 extends WindupConfigurationProvider
    {
        @Override
        public List<Class<? extends WindupConfigurationProvider>> getClassDependencies()
        {
            List<Class<? extends WindupConfigurationProvider>> l = new ArrayList<>();
            l.add(VisitorPhase1Class1.class);
            return l;
        }

        @Override
        public RulePhase getPhase()
        {
            return RulePhase.DISCOVERY;
        }

        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            return null;
        }

        @Override
        public String toString()
        {
            return "Phase1Class2";
        }

        @Override
        public String getID()
        {
            return toString();
        }
    }

    private class VisitorPhase1Class3 extends WindupConfigurationProvider
    {
        @Override
        public List<Class<? extends WindupConfigurationProvider>> getClassDependencies()
        {
            List<Class<? extends WindupConfigurationProvider>> l = new ArrayList<>();
            l.add(VisitorPhase1Class2.class);
            return l;
        }

        @Override
        public RulePhase getPhase()
        {
            return RulePhase.DISCOVERY;
        }

        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            return null;
        }

        @Override
        public String toString()
        {
            return "Phase1Class3";
        }

        @Override
        public String getID()
        {
            return toString();
        }
    }

    private class VisitorPhase2Class1 extends WindupConfigurationProvider
    {
        @Override
        public RulePhase getPhase()
        {
            return RulePhase.INITIAL_ANALYSIS;
        }

        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String toString()
        {
            return "Phase2Class1";
        }

        @Override
        public String getID()
        {
            return toString();
        }
    }

    @Test
    public void testSort()
    {
        WindupConfigurationProvider v1 = new VisitorPhase1Class1();
        WindupConfigurationProvider v2 = new VisitorPhase1Class2();
        WindupConfigurationProvider v3 = new VisitorPhase1Class3();
        WindupConfigurationProvider v4 = new VisitorPhase2Class1();
        List<WindupConfigurationProvider> visitors = new ArrayList<>();
        visitors.add(v3);
        visitors.add(v4);
        visitors.add(v2);
        visitors.add(v1);

        GraphProviderSorter sorter = new GraphProviderSorter();
        List<WindupConfigurationProvider> sortedVisitors = sorter.sort(visitors);

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
        WindupConfigurationProvider v2 = new VisitorPhase1Class2();
        WindupConfigurationProvider v3 = new VisitorPhase1Class3();
        WindupConfigurationProvider v4 = new VisitorPhase2Class1();
        List<WindupConfigurationProvider> visitors = new ArrayList<WindupConfigurationProvider>();
        visitors.add(v3);
        visitors.add(v4);
        visitors.add(v2);
        visitors.add(v1);

        GraphProviderSorter sorter = new GraphProviderSorter();
        try
        {
            List<WindupConfigurationProvider> sortedVisitors = sorter.sort(visitors);
            Assert.fail("No cycles detected");
        }
        catch (RuntimeException e)
        {
            Assert.assertTrue(e.getMessage().contains("Dependency cycles detected"));
        }
    }

}
