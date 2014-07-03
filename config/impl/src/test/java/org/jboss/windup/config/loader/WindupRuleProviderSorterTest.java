package org.jboss.windup.config.loader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.graph.GraphContext;
import org.junit.Assert;
import org.junit.Test;
import org.ocpsoft.rewrite.config.Configuration;

public class WindupRuleProviderSorterTest
{

    private class WCPPhase1Class1 extends WindupRuleProvider
    {
        private List<Class<? extends WindupRuleProvider>> deps = new ArrayList<>();

        @Override
        public List<Class<? extends WindupRuleProvider>> getClassDependencies()
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

    private class WCPPhase1Class2 extends WindupRuleProvider
    {
        @Override
        public List<Class<? extends WindupRuleProvider>> getClassDependencies()
        {
            List<Class<? extends WindupRuleProvider>> l = new ArrayList<>();
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

    private class WCPPhase1Class3 extends WindupRuleProvider
    {
        @Override
        public List<Class<? extends WindupRuleProvider>> getClassDependencies()
        {
            List<Class<? extends WindupRuleProvider>> l = new ArrayList<>();
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

    private class WCPPhase2Class1 extends WindupRuleProvider
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

    private class WCPPhase2Class2 extends WindupRuleProvider
    {
        @Override
        public RulePhase getPhase()
        {
            return RulePhase.INITIAL_ANALYSIS;
        }

        @Override
        public List<String> getIDDependencies()
        {
            return Arrays.asList(new String[] { "Phase2Class1" });
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
            return "Phase2Class2";
        }

        @Override
        public String getID()
        {
            return toString();
        }
    }

    private class WCPPhase2Class3 extends WindupRuleProvider
    {
        @Override
        public RulePhase getPhase()
        {
            return RulePhase.INITIAL_ANALYSIS;
        }

        @Override
        public List<String> getIDDependencies()
        {
            return Arrays.asList(new String[] { "Phase2Class2" });
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
            return "Phase2Class3";
        }

        @Override
        public String getID()
        {
            return toString();
        }
    }

    private class WCPPhase1WrongPhaseDep extends WindupRuleProvider
    {
        @Override
        public RulePhase getPhase()
        {
            return RulePhase.DISCOVERY;
        }

        @Override
        public List<Class<? extends WindupRuleProvider>> getClassDependencies()
        {
            return generateDependencies(WCPPhase2Class1.class);
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
            return "Phase1WrongPhaseDep";
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
        WindupRuleProvider v1 = new WCPPhase1Class1();
        WindupRuleProvider v2 = new WCPPhase1Class2();
        WindupRuleProvider v3 = new WCPPhase1Class3();
        WindupRuleProvider v4 = new WCPPhase2Class1();
        WindupRuleProvider v5 = new WCPPhase2Class2();
        WindupRuleProvider v6 = new WCPPhase2Class3();
        List<WindupRuleProvider> ruleProviders = new ArrayList<>();
        ruleProviders.add(v6);
        ruleProviders.add(v5);
        ruleProviders.add(v3);
        ruleProviders.add(v4);
        ruleProviders.add(v2);
        ruleProviders.add(v1);

        List<WindupRuleProvider> sortedRCPList = WindupRuleProviderSorter
                    .sort(ruleProviders);

        System.out.println("Results: " + sortedRCPList);

        Assert.assertEquals(v1, sortedRCPList.get(0));
        Assert.assertEquals(v2, sortedRCPList.get(1));
        Assert.assertEquals(v3, sortedRCPList.get(2));
        Assert.assertEquals(v4, sortedRCPList.get(3));
        Assert.assertEquals(v5, sortedRCPList.get(4));
        Assert.assertEquals(v6, sortedRCPList.get(5));
    }

    @Test
    public void testSortCycle()
    {
        WCPPhase1Class1 v1 = new WCPPhase1Class1();
        v1.deps.add(WCPPhase1Class3.class);
        WindupRuleProvider v2 = new WCPPhase1Class2();
        WindupRuleProvider v3 = new WCPPhase1Class3();
        WindupRuleProvider v4 = new WCPPhase2Class1();
        List<WindupRuleProvider> ruleProviders = new ArrayList<WindupRuleProvider>();
        ruleProviders.add(v3);
        ruleProviders.add(v4);
        ruleProviders.add(v2);
        ruleProviders.add(v1);

        try
        {
            List<WindupRuleProvider> sortedRCPList = WindupRuleProviderSorter
                        .sort(ruleProviders);
            Assert.fail("No cycles detected");
        }
        catch (RuntimeException e)
        {
            Assert.assertTrue(e.getMessage().contains("Dependency cycles detected"));
        }
    }

    @Test
    public void testCrossPhaseDependency()
    {
        WindupRuleProvider v1 = new WCPPhase1Class1();
        WindupRuleProvider v2 = new WCPPhase1Class2();
        WindupRuleProvider v3 = new WCPPhase1Class3();
        WindupRuleProvider v4 = new WCPPhase2Class1();
        WindupRuleProvider v5 = new WCPPhase2Class2();
        WindupRuleProvider v6 = new WCPPhase2Class3();
        WindupRuleProvider wrongPhaseDep = new WCPPhase1WrongPhaseDep();
        List<WindupRuleProvider> ruleProviders = new ArrayList<>();
        ruleProviders.add(v6);
        ruleProviders.add(v5);
        ruleProviders.add(v3);
        ruleProviders.add(v4);
        ruleProviders.add(v2);
        ruleProviders.add(v1);
        ruleProviders.add(wrongPhaseDep);

        try
        {
            List<WindupRuleProvider> sortedRCPList = WindupRuleProviderSorter
                        .sort(ruleProviders);
            Assert.fail("No improper phase dependencies detected!");
        }
        catch (IncorrectPhaseDependencyException ipde)
        {
            // ignore... this exception is expected in this test
        }
    }

}
