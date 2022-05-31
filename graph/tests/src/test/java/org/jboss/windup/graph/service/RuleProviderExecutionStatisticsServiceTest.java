package org.jboss.windup.graph.service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.performance.RuleProviderExecutionStatisticsModel;
import org.jboss.windup.graph.typedgraph.TestFooModel;
import org.jboss.windup.graph.typedgraph.TestFooSubModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Iterator;

@RunWith(Arquillian.class)
public class RuleProviderExecutionStatisticsServiceTest {
    @Inject
    private GraphContextFactory factory;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClasses(TestFooModel.class, TestFooSubModel.class);
        return archive;
    }

    @Test
    public void testFindAllOrderedByIndex() throws Exception {
        try (GraphContext context = factory.create(true)) {
            RuleProviderExecutionStatisticsService service = new RuleProviderExecutionStatisticsService(context);
            RuleProviderExecutionStatisticsModel m1 = service.create();
            m1.setRuleIndex(10);
            m1.setTimeTaken(1);
            RuleProviderExecutionStatisticsModel m2 = service.create();
            m2.setRuleIndex(20);
            m2.setTimeTaken(2);
            RuleProviderExecutionStatisticsModel m3 = service.create();
            m3.setRuleIndex(30);
            m3.setTimeTaken(3);
            RuleProviderExecutionStatisticsModel m4 = service.create();
            m4.setRuleIndex(40);
            m4.setTimeTaken(4);
            RuleProviderExecutionStatisticsModel m5 = service.create();
            m5.setRuleIndex(50);
            m5.setTimeTaken(5);

            Iterator<RuleProviderExecutionStatisticsModel> i = service.findAllOrderedByIndex().iterator();
            Assert.assertEquals(1, i.next().getTimeTaken());
            Assert.assertEquals(2, i.next().getTimeTaken());
            Assert.assertEquals(3, i.next().getTimeTaken());
            Assert.assertEquals(4, i.next().getTimeTaken());
            Assert.assertEquals(5, i.next().getTimeTaken());
        }
    }

}
