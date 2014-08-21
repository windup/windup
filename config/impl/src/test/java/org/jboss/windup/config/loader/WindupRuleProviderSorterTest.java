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
        public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
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
        public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
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
        public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
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

    private class WCPPhase2Class3 extends WindupRuleProvider
    {
        @Override
        public RulePhase getPhase()
        {
            return RulePhase.INITIAL_ANALYSIS;
        }

        @Override
        public List<String> getExecuteAfterIDs()
        {
            return Arrays.asList(new String[] { "WCPImplicitPhase2Step2" });
        }

        @Override
        public List<Class<? extends WindupRuleProvider>> getExecuteBefore()
        {
            return generateDependencies(WCPPhase2Class4.class);
        }

        @Override
        public Configuration getConfiguration(GraphContext context)
        {
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

    private class WCPPhase2Class4 extends WindupRuleProvider
    {
        @Override
        public RulePhase getPhase()
        {
            return RulePhase.INITIAL_ANALYSIS;
        }

        @Override
        public List<String> getExecuteAfterIDs()
        {
            return Arrays.asList(new String[] { "Phase2Class3" });
        }

        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            return null;
        }

        @Override
        public String toString()
        {
            return "Phase2Class4";
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
        public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
        {
            return generateDependencies(WCPPhase2Class1.class);
        }

        @Override
        public Configuration getConfiguration(GraphContext context)
        {
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

    private class WCPAcceptableCrossPhaseDep extends WindupRuleProvider
    {
        @Override
        public RulePhase getPhase()
        {
            return RulePhase.REPORT_RENDERING;
        }

        @Override
        public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
        {
            return generateDependencies(WCPPhase2Class1.class);
        }

        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            return null;
        }

        @Override
        public String toString()
        {
            return "Phase1AcceptableCrossPhaseDep";
        }

        @Override
        public String getID()
        {
            return toString();
        }
    }

    private class WCPImplicitPhase2Step2 extends WindupRuleProvider
    {
        @Override
        public RulePhase getPhase()
        {
            return null;
        }

        @Override
        public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
        {
            return generateDependencies(WCPPhase2Class1.class);
        }

        @Override
        public List<Class<? extends WindupRuleProvider>> getExecuteBefore()
        {
            return generateDependencies(WCPPhase2Class3.class);
        }

        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            return null;
        }

        @Override
        public String toString()
        {
            return "WCPImplicitPhase2Step2";
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
        WindupRuleProvider v5 = new WCPImplicitPhase2Step2();
        WindupRuleProvider v6 = new WCPPhase2Class3();
        WindupRuleProvider v7 = new WCPPhase2Class4();
        List<WindupRuleProvider> ruleProviders = new ArrayList<>();
        ruleProviders.add(v7);
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
        Assert.assertEquals(v7, sortedRCPList.get(6));
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
    public void testImproperCrossPhaseDependency()
    {
        WindupRuleProvider v1 = new WCPPhase1Class1();
        WindupRuleProvider v2 = new WCPPhase1Class2();
        WindupRuleProvider v3 = new WCPPhase1Class3();
        WindupRuleProvider v4 = new WCPPhase2Class1();
        WindupRuleProvider v5 = new WCPPhase2Class3();
        WindupRuleProvider v6 = new WCPPhase2Class4();
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

    @Test
    public void testAcceptableCrossPhaseDependency()
    {
        WindupRuleProvider v1 = new WCPPhase1Class1();
        WindupRuleProvider v2 = new WCPPhase1Class2();
        WindupRuleProvider v3 = new WCPPhase1Class3();
        WindupRuleProvider v4 = new WCPPhase2Class1();
        WindupRuleProvider v5 = new WCPImplicitPhase2Step2();
        WindupRuleProvider v6 = new WCPPhase2Class3();
        WindupRuleProvider v7 = new WCPPhase2Class4();
        WindupRuleProvider acceptablePhaseDep = new WCPAcceptableCrossPhaseDep();
        List<WindupRuleProvider> ruleProviders = new ArrayList<>();
        ruleProviders.add(v7);
        ruleProviders.add(v6);
        ruleProviders.add(v5);
        ruleProviders.add(v3);
        ruleProviders.add(v4);
        ruleProviders.add(v2);
        ruleProviders.add(v1);
        ruleProviders.add(acceptablePhaseDep);

        try
        {
            List<WindupRuleProvider> sortedRCPList = WindupRuleProviderSorter
                        .sort(ruleProviders);

        }
        catch (IncorrectPhaseDependencyException ipde)
        {
            ipde.printStackTrace();
            Assert.fail("This cross-dependency should be acceptable!");
        }
    }

}
