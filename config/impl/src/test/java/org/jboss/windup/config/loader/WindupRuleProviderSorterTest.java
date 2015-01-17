package org.jboss.windup.config.loader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.phase.Implicit;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.util.exception.WindupMultiStringException;
import org.junit.Assert;
import org.junit.Test;
import org.ocpsoft.rewrite.config.Configuration;

public class WindupRuleProviderSorterTest
{
    private class Phase1 extends RulePhase
    {
        @Override
        public List<Class<? extends WindupRuleProvider>> getExecuteBefore()
        {
            return asClassList(Phase2.class);
        }
    }

    private class Phase2 extends RulePhase
    {
        @Override
        public List<Class<? extends WindupRuleProvider>> getExecuteBefore()
        {
            return asClassList(Phase3.class);
        }
    }

    private class Phase3 extends RulePhase
    {
    }

    private class Phase4 extends RulePhase
    {
        @Override
        public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
        {
            return asClassList(Phase3.class);
        }
    }

    private class WCPPhase1Class1 extends WindupRuleProvider
    {
        private List<Class<? extends WindupRuleProvider>> deps = new ArrayList<>();

        @Override
        public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
        {
            return deps;
        }

        @Override
        public Class<? extends RulePhase> getPhase()
        {
            return Phase1.class;
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
        public Class<? extends RulePhase> getPhase()
        {
            return Phase1.class;
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

    private class WCPPhaseImplicitClass2 extends WindupRuleProvider
    {
        @Override
        public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
        {
            List<Class<? extends WindupRuleProvider>> l = new ArrayList<>();
            l.add(WCPPhase1Class2.class);
            return l;
        }

        @Override
        public List<Class<? extends WindupRuleProvider>> getExecuteBefore()
        {
            return asClassList(WCPPhase2Class1.class);
        }

        @Override
        public Class<? extends RulePhase> getPhase()
        {
            return Implicit.class;
        }

        @Override
        public Configuration getConfiguration(GraphContext context)
        {
            return null;
        }

        @Override
        public String toString()
        {
            return "PhaseImplicitClass2";
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
        public Class<? extends RulePhase> getPhase()
        {
            return Phase1.class;
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
        public Class<? extends RulePhase> getPhase()
        {
            return Phase2.class;
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
        public Class<? extends RulePhase> getPhase()
        {
            return Phase2.class;
        }

        @Override
        public List<String> getExecuteAfterIDs()
        {
            return Arrays.asList(new String[] { "WCPImplicitPhase2Step2" });
        }

        @Override
        public List<Class<? extends WindupRuleProvider>> getExecuteBefore()
        {
            return asClassList(WCPPhase2Class4.class);
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
        public Class<? extends RulePhase> getPhase()
        {
            return Phase2.class;
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
        public Class<? extends RulePhase> getPhase()
        {
            return Phase1.class;
        }

        @Override
        public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
        {
            return asClassList(WCPPhase2Class1.class);
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
        public Class<? extends RulePhase> getPhase()
        {
            return Phase3.class;
        }

        @Override
        public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
        {
            return asClassList(WCPPhase2Class1.class);
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
        public Class<? extends RulePhase> getPhase()
        {
            return Implicit.class;
        }

        @Override
        public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
        {
            return asClassList(WCPPhase2Class1.class);
        }

        @Override
        public List<Class<? extends WindupRuleProvider>> getExecuteBefore()
        {
            return asClassList(WCPPhase2Class3.class);
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

    private List<RulePhase> getPhases()
    {
        List<RulePhase> phases = new ArrayList<>();
        // mix them up as we want to test sorting of these as well
        phases.add(new Phase3());
        phases.add(new Phase1());
        phases.add(new Phase4());
        phases.add(new Phase2());
        return phases;
    }

    @Test
    public void testSort()
    {
        WindupRuleProvider v1 = new WCPPhase1Class1();
        WindupRuleProvider v2 = new WCPPhase1Class2();
        WindupRuleProvider vI = new WCPPhaseImplicitClass2();
        WindupRuleProvider v3 = new WCPPhase1Class3();
        WindupRuleProvider v4 = new WCPPhase2Class1();
        WindupRuleProvider v5 = new WCPImplicitPhase2Step2();
        WindupRuleProvider v6 = new WCPPhase2Class3();
        WindupRuleProvider v7 = new WCPPhase2Class4();
        List<WindupRuleProvider> ruleProviders = new ArrayList<>();
        ruleProviders.add(v7);
        ruleProviders.add(v6);
        ruleProviders.add(vI);
        ruleProviders.add(v5);
        ruleProviders.add(v3);
        ruleProviders.add(v4);
        ruleProviders.add(v2);
        ruleProviders.add(v1);
        ruleProviders.addAll(getPhases());

        List<WindupRuleProvider> sortedRCPListUnmodifiable = WindupRuleProviderSorter
                    .sort(ruleProviders);
        System.out.println("Results With Phases:  " + sortedRCPListUnmodifiable);
        List<WindupRuleProvider> sortedRCPList = new ArrayList<>(sortedRCPListUnmodifiable);

        // remove phases (this makes asserting on the results easier)
        ListIterator<WindupRuleProvider> sortedRCPLI = sortedRCPList.listIterator();
        while (sortedRCPLI.hasNext())
        {
            WindupRuleProvider p = sortedRCPLI.next();
            if (p instanceof RulePhase)
            {
                sortedRCPLI.remove();
            }
        }
        System.out.println("Results without Phases:  " + sortedRCPList);

        Assert.assertEquals(v1, sortedRCPList.get(0));
        Assert.assertEquals(v2, sortedRCPList.get(1));
        Assert.assertEquals(vI, sortedRCPList.get(2));
        Assert.assertEquals(v3, sortedRCPList.get(3));
        Assert.assertEquals(v4, sortedRCPList.get(4));
        Assert.assertEquals(v5, sortedRCPList.get(5));
        Assert.assertEquals(v6, sortedRCPList.get(6));
        Assert.assertEquals(v7, sortedRCPList.get(7));
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
            WindupRuleProviderSorter.sort(ruleProviders);
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
            WindupRuleProviderSorter.sort(ruleProviders);
            Assert.fail("No improper phase dependencies detected!");
        }
        catch (IncorrectPhaseDependencyException | WindupMultiStringException e)
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
            WindupRuleProviderSorter.sort(ruleProviders);
        }
        catch (IncorrectPhaseDependencyException e)
        {
            e.printStackTrace();
            Assert.fail("This cross-dependency should be acceptable!");
        }
    }

    @Test
    public void testPhaseSorting()
    {
        List<WindupRuleProvider> ruleProviders = new ArrayList<>();
        ruleProviders.addAll(getPhases());

        List<WindupRuleProvider> results = WindupRuleProviderSorter.sort(ruleProviders);
        Assert.assertEquals(4, results.size());

        int row = 0;
        Assert.assertTrue(results.get(row) instanceof Phase1);
        Assert.assertTrue(results.get(row).getExecuteAfter().isEmpty());
        Assert.assertEquals(1, results.get(row).getExecuteBefore().size());
        Assert.assertTrue(results.get(row).getExecuteBefore().get(0) == Phase2.class);

        row++;
        Assert.assertTrue(results.get(row) instanceof Phase2);
        Assert.assertEquals(1, results.get(row).getExecuteAfter().size());
        Assert.assertEquals(1, results.get(row).getExecuteBefore().size());
        Assert.assertTrue(results.get(row).getExecuteAfter().get(0) == Phase1.class);
        Assert.assertTrue(results.get(row).getExecuteBefore().get(0) == Phase3.class);

        row++;
        Assert.assertTrue(results.get(row) instanceof Phase3);
        Assert.assertEquals(1, results.get(row).getExecuteAfter().size());
        Assert.assertEquals(1, results.get(row).getExecuteBefore().size());
        Assert.assertTrue(results.get(row).getExecuteAfter().get(0) == Phase2.class);
        Assert.assertTrue(results.get(row).getExecuteBefore().get(0) == Phase4.class);

        row++;
        Assert.assertTrue(results.get(row) instanceof Phase4);
        Assert.assertEquals(1, results.get(row).getExecuteAfter().size());
        Assert.assertEquals(0, results.get(row).getExecuteBefore().size());
        Assert.assertTrue(results.get(row).getExecuteAfter().get(0) == Phase3.class);
        Assert.assertTrue(results.get(row).getExecuteBefore().isEmpty());
    }

}
