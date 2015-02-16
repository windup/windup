package org.jboss.windup.config.parser;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.phase.Discovery;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.And;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.DefaultOperationBuilder;
import org.ocpsoft.rewrite.config.Not;
import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.config.RuleBuilder;
import org.ocpsoft.rewrite.config.True;
import org.ocpsoft.rewrite.param.Parameter;
import org.ocpsoft.rewrite.param.RegexConstraint;

@RunWith(Arquillian.class)
public class XMLRuleProviderLoaderTest
{
    private static Logger LOG = Logger.getLogger(XMLRuleProviderLoaderTest.class.getName());

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.config:windup-config-xml"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsResource(new File("src/test/resources/testxml/Test1.windup.xml"))
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config-xml"),
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private XMLRuleProviderLoader loader;
    @Inject
    private GraphContextFactory graphContextFactory;

    @Test
    public void testGetProviders() throws Exception
    {
        Assert.assertNotNull(loader);

        try (GraphContext graphContext = graphContextFactory.create())
        {
            List<WindupRuleProvider> providers = loader.getProviders(graphContext);
            Assert.assertNotNull(providers);
            Assert.assertTrue(providers.size() == 1);

            WindupRuleProvider provider = providers.get(0);
            String id = provider.getID();
            Assert.assertEquals("testruleprovider", id);
            Assert.assertEquals(Discovery.class, provider.getPhase());
            List<Rule> rules = provider.getConfiguration(graphContext).getRules();
            Assert.assertEquals(3, rules.size());

            RuleBuilder rule1 = (RuleBuilder) rules.get(0);
            checkRule1(rule1);

            RuleBuilder rule2 = (RuleBuilder) rules.get(1);
            checkRule2(rule2);

            RuleBuilder rule2_otherwise = (RuleBuilder) rules.get(2);
            checkRule2_Otherwise(rule2_otherwise);
        }
    }

    private void checkRule1(RuleBuilder rule)
    {
        // check the conditions
        List<Condition> conditions = rule.getConditions();
        Assert.assertEquals(1, conditions.size());
        Condition condition = conditions.get(0);
        Assert.assertTrue(condition instanceof And);
        And and = (And) condition;
        Assert.assertEquals(1, and.getConditions().size());
        Assert.assertTrue(and.getConditions().get(0) instanceof True);

        // check the operations
        List<Operation> operations = rule.getOperations();
        Assert.assertEquals(1, operations.size());
        Assert.assertTrue(operations.get(0) instanceof DefaultOperationBuilder);
    }

    private void checkRule2(RuleBuilder rule) throws Exception
    {
        LOG.info("Rule: " + rule);

        // check the conditions
        List<Condition> conditions = rule.getConditions();
        Assert.assertEquals(1, conditions.size());
        Condition condition = conditions.get(0);
        Assert.assertTrue(condition instanceof And);
        And and = (And) condition;
        Assert.assertEquals(1, and.getConditions().size());
        Assert.assertTrue(and.getConditions().get(0) instanceof True);

        // check the operations
        List<Operation> operations = rule.getOperations();
        Assert.assertEquals(1, operations.size());
        Assert.assertTrue(operations.get(0) instanceof DefaultOperationBuilder);
        DefaultOperationBuilder opBuilder = (DefaultOperationBuilder) operations.get(0);
        String opBuilderStr = opBuilder.toString();
        LOG.info("Op Builder is: " + opBuilderStr);

        Assert.assertTrue(opBuilderStr.contains("over(?).when(new True"));
        Assert.assertTrue(opBuilderStr.contains("perform(Perform.all(LOG[INFO, test {foo} iteration perform]))"));
        Assert.assertTrue(opBuilderStr.contains("otherwise(Perform.all(LOG[INFO, test {foo} iteration otherwise]"));

        Parameter<?> foo = rule.getParameterStore().get("foo");
        Assert.assertEquals("foo", foo.getName());
        Assert.assertEquals(new RegexConstraint("\\d+"), foo.getConstraints().get(0));
    }

    private void checkRule2_Otherwise(RuleBuilder rule) throws Exception
    {
        LOG.info("Rule: " + rule);

        // check the conditions
        List<Condition> conditions = rule.getConditions();
        Assert.assertEquals(1, conditions.size());
        Condition condition = conditions.get(0);
        Assert.assertTrue(condition instanceof Not);
        Condition andCondition = ((RuleBuilder) ((Not) condition).getConditions().get(0)).getConditions().get(0);
        Assert.assertTrue(andCondition instanceof And);
        And and = (And) andCondition;
        Assert.assertEquals(1, and.getConditions().size());
        Assert.assertTrue(and.getConditions().get(0) instanceof True);

        // check the operations
        List<Operation> operations = rule.getOperations();
        Assert.assertEquals(1, operations.size());
        Assert.assertTrue(operations.get(0) instanceof DefaultOperationBuilder);
        DefaultOperationBuilder opBuilder = (DefaultOperationBuilder) operations.get(0);
        String opBuilderStr = opBuilder.toString();
        LOG.info("Op Builder is: " + opBuilderStr);

        Assert.assertTrue(opBuilderStr.contains("LOG[INFO, test rule {foo} otherwise]"));
    }
}
