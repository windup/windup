package org.jboss.windup.config.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.phase.DependentPhase;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.util.exception.WindupMultiStringException;
import org.junit.Assert;
import org.junit.Test;
import org.ocpsoft.rewrite.config.Configuration;

public class RuleProviderSorterTest {
    private class Phase1 extends RulePhase {
        public Phase1() {
            super(Phase1.class);
        }

        @Override
        public Class<? extends RulePhase> getExecuteBefore() {
            return Phase2.class;
        }

        @Override
        public Class<? extends RulePhase> getExecuteAfter() {
            return null;
        }
    }

    private class Phase2 extends RulePhase {
        public Phase2() {
            super(Phase2.class);
        }

        @Override
        public Class<? extends RulePhase> getExecuteBefore() {
            return Phase3.class;
        }

        @Override
        public Class<? extends RulePhase> getExecuteAfter() {
            return null;
        }
    }

    private class Phase3 extends RulePhase {
        public Phase3() {
            super(Phase3.class);
        }

        @Override
        public Class<? extends RulePhase> getExecuteBefore() {
            return null;
        }

        @Override
        public Class<? extends RulePhase> getExecuteAfter() {
            return null;
        }
    }

    private class Phase4 extends RulePhase {
        public Phase4() {
            super(Phase4.class);
        }

        @Override
        public Class<? extends RulePhase> getExecuteBefore() {
            return null;
        }

        @Override
        public Class<? extends RulePhase> getExecuteAfter() {
            return Phase3.class;
        }
    }

    private static class WCPPhase1Class1 extends AbstractRuleProvider {
        public WCPPhase1Class1() {
            super(MetadataBuilder.forProvider(WCPPhase1Class1.class)
                    .setPhase(Phase1.class));
        }

        public WCPPhase1Class1(Class<? extends RuleProvider> dependency) {
            super(MetadataBuilder.forProvider(WCPPhase1Class1.class)
                    .addExecuteAfter(dependency)
                    .setPhase(Phase1.class));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return null;
        }
    }

    private class WCPPhase1Class2 extends AbstractRuleProvider {
        public WCPPhase1Class2() {
            super(MetadataBuilder.forProvider(WCPPhase1Class2.class)
                    .setPhase(Phase1.class)
                    .addExecuteAfter(WCPPhase1Class1.class));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return null;
        }
    }

    private class WCPPhaseDependentClass2 extends AbstractRuleProvider {
        public WCPPhaseDependentClass2() {
            super(MetadataBuilder.forProvider(WCPPhaseDependentClass2.class)
                    .addExecuteAfter(WCPPhase1Class2.class));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return null;
        }
    }

    private class WCPPhase1Class3 extends AbstractRuleProvider {
        public WCPPhase1Class3() {
            super(MetadataBuilder.forProvider(WCPPhase1Class3.class)
                    .addExecuteAfter(WCPPhase1Class2.class)
                    .setPhase(Phase1.class));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return null;
        }
    }

    private class WCPPhase2Class1 extends AbstractRuleProvider {
        public WCPPhase2Class1() {
            super(MetadataBuilder.forProvider(WCPPhase2Class1.class)
                    .setPhase(Phase2.class));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return null;
        }

    }

    private class WCPPhase2Class3 extends AbstractRuleProvider {
        public WCPPhase2Class3() {
            super(MetadataBuilder.forProvider(WCPPhase2Class3.class)
                    .addExecuteAfterId("WCPDependentPhase2Step2")
                    .setPhase(Phase2.class));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return null;
        }
    }

    private class WCPPhase2Class4 extends AbstractRuleProvider {
        public WCPPhase2Class4() {
            super(MetadataBuilder.forProvider(WCPPhase2Class4.class)
                    .addExecuteAfterId("WCPPhase2Class3")
                    .setPhase(Phase2.class));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return null;
        }
    }

    private class WCPPhase1WrongPhaseDep extends AbstractRuleProvider {
        public WCPPhase1WrongPhaseDep() {
            super(MetadataBuilder.forProvider(WCPPhase1WrongPhaseDep.class)
                    .addExecuteAfter(WCPPhase2Class1.class)
                    .setPhase(Phase1.class));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return null;
        }
    }

    private class WCPAcceptableCrossPhaseDep extends AbstractRuleProvider {
        public WCPAcceptableCrossPhaseDep() {
            super(MetadataBuilder.forProvider(WCPAcceptableCrossPhaseDep.class)
                    .addExecuteAfter(WCPPhase2Class1.class)
                    .setPhase(Phase3.class));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return null;
        }
    }

    private class WCPDependentPhase2Step2 extends AbstractRuleProvider {
        public WCPDependentPhase2Step2() {
            super(MetadataBuilder.forProvider(WCPDependentPhase2Step2.class)
                    .addExecuteAfter(WCPPhase2Class1.class)
                    .addExecuteBefore(WCPPhase2Class3.class)
                    .setPhase(DependentPhase.class));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return null;
        }
    }

    private List<RulePhase> getPhases() {
        List<RulePhase> phases = new ArrayList<>();
        // mix them up as we want to test sorting of these as well
        phases.add(new Phase3());
        phases.add(new Phase1());
        phases.add(new Phase4());
        phases.add(new Phase2());
        return phases;
    }

    @Test
    public void testSort() {
        AbstractRuleProvider v1 = new WCPPhase1Class1();
        AbstractRuleProvider v2 = new WCPPhase1Class2();
        AbstractRuleProvider vI = new WCPPhaseDependentClass2();
        AbstractRuleProvider v3 = new WCPPhase1Class3();
        AbstractRuleProvider v4 = new WCPPhase2Class1();
        AbstractRuleProvider v5 = new WCPDependentPhase2Step2();
        AbstractRuleProvider v6 = new WCPPhase2Class3();
        AbstractRuleProvider v7 = new WCPPhase2Class4();

        List<RuleProvider> ruleProviders = new ArrayList<>();
        ruleProviders.add(v7);
        ruleProviders.add(v6);
        ruleProviders.add(vI);
        ruleProviders.add(v5);
        ruleProviders.add(v3);
        ruleProviders.add(v4);
        ruleProviders.add(v2);
        ruleProviders.add(v1);
        ruleProviders.addAll(getPhases());

        List<RuleProvider> sortedRuleProviders = new ArrayList<>(RuleProviderSorter.sort(ruleProviders));
        System.out.println("Results With Phases:  " + sortedRuleProviders);

        /*
         * Remove phases (this makes asserting on the results easier)
         */
        ListIterator<RuleProvider> iterator = sortedRuleProviders.listIterator();
        while (iterator.hasNext()) {
            RuleProvider p = iterator.next();
            if (p instanceof RulePhase) {
                iterator.remove();
            }
        }
        System.out.println("Results without Phases:  " + sortedRuleProviders);

        Assert.assertEquals(v1, sortedRuleProviders.get(0));
        Assert.assertEquals(v2, sortedRuleProviders.get(1));
        Assert.assertEquals(vI, sortedRuleProviders.get(2));
        Assert.assertEquals(v3, sortedRuleProviders.get(3));
        Assert.assertEquals(v4, sortedRuleProviders.get(4));
        Assert.assertEquals(v5, sortedRuleProviders.get(5));
        Assert.assertEquals(v6, sortedRuleProviders.get(6));
        Assert.assertEquals(v7, sortedRuleProviders.get(7));
    }

    @Test
    public void testSortCycle() {
        WCPPhase1Class1 v1 = new WCPPhase1Class1(WCPPhase1Class3.class);
        AbstractRuleProvider v2 = new WCPPhase1Class2();
        AbstractRuleProvider v3 = new WCPPhase1Class3();
        AbstractRuleProvider v4 = new WCPPhase2Class1();
        List<RuleProvider> ruleProviders = new ArrayList<>();
        ruleProviders.add(v3);
        ruleProviders.add(v4);
        ruleProviders.add(v2);
        ruleProviders.add(v1);

        try {
            RuleProviderSorter.sort(ruleProviders);
            Assert.fail("No cycles detected");
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Dependency cycles detected"));
        }
    }

    @Test
    public void testImproperCrossPhaseDependency() {
        AbstractRuleProvider v1 = new WCPPhase1Class1();
        AbstractRuleProvider v2 = new WCPPhase1Class2();
        AbstractRuleProvider v3 = new WCPPhase1Class3();
        AbstractRuleProvider v4 = new WCPPhase2Class1();
        AbstractRuleProvider v5 = new WCPPhase2Class3();
        AbstractRuleProvider v6 = new WCPPhase2Class4();
        AbstractRuleProvider wrongPhaseDep = new WCPPhase1WrongPhaseDep();

        List<RuleProvider> ruleProviders = new ArrayList<>();
        ruleProviders.add(v6);
        ruleProviders.add(v5);
        ruleProviders.add(v3);
        ruleProviders.add(v4);
        ruleProviders.add(v2);
        ruleProviders.add(v1);
        ruleProviders.add(wrongPhaseDep);

        try {
            RuleProviderSorter.sort(ruleProviders);
            Assert.fail("No improper phase dependencies detected!");
        } catch (IncorrectPhaseDependencyException | WindupMultiStringException e) {
            // ignore... this exception is expected in this test
        }
    }

    @Test
    public void testAcceptableCrossPhaseDependency() {
        AbstractRuleProvider v1 = new WCPPhase1Class1();
        AbstractRuleProvider v2 = new WCPPhase1Class2();
        AbstractRuleProvider v3 = new WCPPhase1Class3();
        AbstractRuleProvider v4 = new WCPPhase2Class1();
        AbstractRuleProvider v5 = new WCPDependentPhase2Step2();
        AbstractRuleProvider v6 = new WCPPhase2Class3();
        AbstractRuleProvider v7 = new WCPPhase2Class4();
        AbstractRuleProvider acceptablePhaseDep = new WCPAcceptableCrossPhaseDep();

        List<RuleProvider> ruleProviders = new ArrayList<>();
        ruleProviders.add(v7);
        ruleProviders.add(v6);
        ruleProviders.add(v5);
        ruleProviders.add(v3);
        ruleProviders.add(v4);
        ruleProviders.add(v2);
        ruleProviders.add(v1);
        ruleProviders.add(acceptablePhaseDep);

        try {
            RuleProviderSorter.sort(ruleProviders);
        } catch (IncorrectPhaseDependencyException e) {
            e.printStackTrace();
            Assert.fail("This cross-dependency should be acceptable!");
        }
    }

    @Test
    public void testPhaseSorting() {
        List<RuleProvider> ruleProviders = new ArrayList<>();
        ruleProviders.addAll(getPhases());

        List<RuleProvider> results = RuleProviderSorter.sort(ruleProviders);
        Assert.assertEquals(4, results.size());

        int row = 0;
        Assert.assertTrue(results.get(row) instanceof Phase1);
        Assert.assertTrue(results.get(row).getMetadata().getExecuteAfter().isEmpty());
        Assert.assertEquals(1, results.get(row).getMetadata().getExecuteBefore().size());
        Assert.assertTrue(results.get(row).getMetadata().getExecuteBefore().get(0) == Phase2.class);

        row++;
        Assert.assertTrue(results.get(row) instanceof Phase2);
        Assert.assertEquals(1, results.get(row).getMetadata().getExecuteAfter().size());
        Assert.assertEquals(1, results.get(row).getMetadata().getExecuteBefore().size());
        Assert.assertTrue(results.get(row).getMetadata().getExecuteAfter().get(0) == Phase1.class);
        Assert.assertTrue(results.get(row).getMetadata().getExecuteBefore().get(0) == Phase3.class);

        row++;
        Assert.assertTrue(results.get(row) instanceof Phase3);
        Assert.assertEquals(1, results.get(row).getMetadata().getExecuteAfter().size());
        Assert.assertEquals(1, results.get(row).getMetadata().getExecuteBefore().size());
        Assert.assertTrue(results.get(row).getMetadata().getExecuteAfter().get(0) == Phase2.class);
        Assert.assertTrue(results.get(row).getMetadata().getExecuteBefore().get(0) == Phase4.class);

        row++;
        Assert.assertTrue(results.get(row) instanceof Phase4);
        Assert.assertEquals(1, results.get(row).getMetadata().getExecuteAfter().size());
        Assert.assertEquals(0, results.get(row).getMetadata().getExecuteBefore().size());
        Assert.assertTrue(results.get(row).getMetadata().getExecuteAfter().get(0) == Phase3.class);
        Assert.assertTrue(results.get(row).getMetadata().getExecuteBefore().isEmpty());
    }

}
