package org.jboss.windup.config.parser;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.builder.RuleProviderBuilder;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.phase.DiscoveryPhase;
import org.jboss.windup.config.phase.ReportGenerationPhase;
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

import javax.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;

@RunWith(Arquillian.class)
public class XMLRuleProviderLoaderTest {
    private static final Logger LOG = Logger.getLogger(XMLRuleProviderLoaderTest.class.getName());
    @Inject
    private XMLRuleProviderLoader loader;
    @Inject
    private GraphContextFactory graphContextFactory;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-xml"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addAsResource(new File("src/test/resources/testxml/Test1.windup.xml"));
    }

    @Deployment(name = "rhamt,1")
    public static AddonArchive getRhamtDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addAsResource(new File("src/test/resources/testxml/Test2.rhamt.xml"));
    }

    @Deployment(name = "mta,1")
    public static AddonArchive getMtaDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addAsResource(new File("src/test/resources/testxml/Test3.mta.xml"));
    }

    @Test
    public void testGetProviders() throws Exception {
        Assert.assertNotNull(loader);

        RuleLoaderContext ruleLoaderContext = new RuleLoaderContext();
        List<RuleProvider> providers = loader.getProviders(ruleLoaderContext);
        Assert.assertNotNull(providers);
        Assert.assertTrue(providers.size() == 3);

        RuleProvider provider = providers.get(providers.indexOf(RuleProviderBuilder.begin("testruleprovider")));
        checkWindupMetadata(provider);
        List<Rule> rules = provider.getConfiguration(null).getRules();
        Assert.assertEquals(4, rules.size());

        RuleBuilder rule1 = (RuleBuilder) rules.get(0);
        checkRule1(rule1);

        RuleBuilder rule2 = (RuleBuilder) rules.get(1);
        checkRule2(rule2);

        RuleBuilder rule2_otherwise = (RuleBuilder) rules.get(2);
        checkRule2_Otherwise(rule2_otherwise);

        RuleBuilder rule3 = (RuleBuilder) rules.get(3);
        checkRule3(rule3);

        provider = providers.get(providers.indexOf(RuleProviderBuilder.begin("testruleprovider2")));
        checkRhamtMetadata(provider);
        rules = provider.getConfiguration(null).getRules();
        Assert.assertEquals(4, rules.size());

        RuleBuilder rule = (RuleBuilder) rules.get(0);
        checkRule1(rule);

        rule = (RuleBuilder) rules.get(1);
        checkRule2(rule);

        rule = (RuleBuilder) rules.get(2);
        checkRule2_Otherwise(rule);

        rule = (RuleBuilder) rules.get(3);
        checkRule3(rule);

        provider = providers.get(providers.indexOf(RuleProviderBuilder.begin("testruleprovider3")));
        checkMtaMetadata(provider);
        rules = provider.getConfiguration(null).getRules();
        Assert.assertEquals(4, rules.size());

        rule = (RuleBuilder) rules.get(0);
        checkRule1(rule);

        rule = (RuleBuilder) rules.get(1);
        checkRule2(rule);

        rule = (RuleBuilder) rules.get(2);
        checkRule2_Otherwise(rule);

        rule = (RuleBuilder) rules.get(3);
        checkRule3(rule);
    }

    private void checkWindupMetadata(RuleProvider provider) {
        String id = provider.getMetadata().getID();
        Assert.assertEquals("testruleprovider", id);
        Assert.assertEquals(DiscoveryPhase.class, provider.getMetadata().getPhase());
        Assert.assertTrue(provider.getMetadata().getOrigin().matches("jar:file:.*/DEFAULT.*/Test1.windup.xml"));
    }

    private void checkRhamtMetadata(RuleProvider provider) {
        String id = provider.getMetadata().getID();
        Assert.assertEquals("testruleprovider2", id);
        Assert.assertEquals(ReportGenerationPhase.class, provider.getMetadata().getPhase());
        Assert.assertTrue(provider.getMetadata().getOrigin().matches("jar:file:.*/rhamt-1.*/Test2.rhamt.xml"));
    }

    private void checkMtaMetadata(RuleProvider provider) {
        String id = provider.getMetadata().getID();
        Assert.assertEquals("testruleprovider3", id);
        Assert.assertEquals(ReportGenerationPhase.class, provider.getMetadata().getPhase());
        Assert.assertTrue(provider.getMetadata().getOrigin().matches("jar:file:.*/mta-1.*/Test3.mta.xml"));
    }

    private void checkRule1(RuleBuilder rule) {
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

    private void checkRule2(RuleBuilder rule) throws Exception {
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

    private void checkRule2_Otherwise(RuleBuilder rule) throws Exception {
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

    private void checkRule3(RuleBuilder rule) {
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

        Assert.assertTrue(opBuilderStr.contains("LOG[INFO, subsetperform"));
        Assert.assertTrue(opBuilderStr.contains("and(RuleSubset.create"));
    }
}
