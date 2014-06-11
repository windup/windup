package org.jboss.windup.config.loader;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupConfigurationProvider;
import org.jboss.windup.graph.GraphContext;
import org.junit.Assert;
import org.junit.Test;
import org.ocpsoft.rewrite.config.Configuration;

public class WindupConfigurationProviderSorterTest
{

    private class WCPPhase1Class1 extends WindupConfigurationProvider
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

    private class WCPPhase1Class2 extends WindupConfigurationProvider
    {
        @Override
        public List<Class<? extends WindupConfigurationProvider>> getClassDependencies()
        {
            List<Class<? extends WindupConfigurationProvider>> l = new ArrayList<>();
            l.add(WCPPhase1Class1.class);
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

    private class WCPPhase1Class3 extends WindupConfigurationProvider
    {
        @Override
        public List<Class<? extends WindupConfigurationProvider>> getClassDependencies()
        {
            List<Class<? extends WindupConfigurationProvider>> l = new ArrayList<>();
            l.add(WCPPhase1Class2.class);
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

    private class WCPPhase2Class1 extends WindupConfigurationProvider
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
        WindupConfigurationProvider v1 = new WCPPhase1Class1();
        WindupConfigurationProvider v2 = new WCPPhase1Class2();
        WindupConfigurationProvider v3 = new WCPPhase1Class3();
        WindupConfigurationProvider v4 = new WCPPhase2Class1();
        List<WindupConfigurationProvider> configurationProviders = new ArrayList<>();
        configurationProviders.add(v3);
        configurationProviders.add(v4);
        configurationProviders.add(v2);
        configurationProviders.add(v1);

        List<WindupConfigurationProvider> sortedWCPList = WindupConfigurationProviderSorter
                    .sort(configurationProviders);

        System.out.println("Results: " + sortedWCPList);

        Assert.assertEquals(v1, sortedWCPList.get(0));
        Assert.assertEquals(v2, sortedWCPList.get(1));
        Assert.assertEquals(v3, sortedWCPList.get(2));
        Assert.assertEquals(v4, sortedWCPList.get(3));
    }

    @Test
    public void testSortCycle()
    {
        WCPPhase1Class1 v1 = new WCPPhase1Class1();
        v1.deps.add(WCPPhase1Class3.class);
        WindupConfigurationProvider v2 = new WCPPhase1Class2();
        WindupConfigurationProvider v3 = new WCPPhase1Class3();
        WindupConfigurationProvider v4 = new WCPPhase2Class1();
        List<WindupConfigurationProvider> configurationProviders = new ArrayList<WindupConfigurationProvider>();
        configurationProviders.add(v3);
        configurationProviders.add(v4);
        configurationProviders.add(v2);
        configurationProviders.add(v1);

        try
        {
            List<WindupConfigurationProvider> sortedWCPList = WindupConfigurationProviderSorter
                        .sort(configurationProviders);
            Assert.fail("No cycles detected");
        }
        catch (RuntimeException e)
        {
            Assert.assertTrue(e.getMessage().contains("Dependency cycles detected"));
        }
    }

}
