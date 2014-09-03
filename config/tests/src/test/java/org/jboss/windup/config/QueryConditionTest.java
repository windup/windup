package org.jboss.windup.config;

import java.nio.file.Path;
import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.model.TestSomeModel;
import org.jboss.windup.config.model.TestXmlMetaFacetModel;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaMethodModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

@RunWith(Arquillian.class)
public class QueryConditionTest
{
    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClasses(TestMavenExampleRuleProvider.class,
                                TestJavaExampleRuleProvider.class,
                                TestXmlExampleRuleProvider1.class,
                                TestXmlExampleRuleProvider2.class,
                                TestXmlExampleRuleProvider3.class,
                                TestGremlinQueryOnlyRuleProvider.class,
                                TestXmlMetaFacetModel.class,
                                TestSomeModel.class,
                                TestWindupConfigurationExampleRuleProvider.class)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private GraphContextFactory factory;

    private DefaultEvaluationContext createEvalContext(GraphRewrite event)
    {
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);
        return evaluationContext;
    }

    private void fillData(final GraphContext context)
    {
        context.getFramed().addVertex(null, TestSomeModel.class);
        context.getFramed().addVertex(null, TestSomeModel.class);
        context.getFramed().addVertex(null, TestSomeModel.class);
        context.getFramed().addVertex(null, TestSomeModel.class);

        TestXmlMetaFacetModel xmlFacet1 = context.getFramed().addVertex(null, TestXmlMetaFacetModel.class);
        xmlFacet1.setRootTagName("xmlTag1");
        TestXmlMetaFacetModel xmlFacet2 = context.getFramed().addVertex(null, TestXmlMetaFacetModel.class);
        xmlFacet2.setRootTagName("xmlTag2");
        TestXmlMetaFacetModel xmlFacet3 = context.getFramed().addVertex(null, TestXmlMetaFacetModel.class);
        xmlFacet3.setRootTagName("xmlTag3");
        TestXmlMetaFacetModel xmlFacet4 = context.getFramed().addVertex(null, TestXmlMetaFacetModel.class);
        xmlFacet4.setRootTagName("xmlTag4");
    }

    @Test
    public void testInitialQueryAsGremlin()
    {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        final GraphContext context = factory.create(folder);

        GraphRewrite event = new GraphRewrite(context);
        DefaultEvaluationContext evaluationContext = createEvalContext(event);

        WindupConfigurationModel windupCfg = context.getFramed().addVertex(null, WindupConfigurationModel.class);
        windupCfg.setInputPath(folder.toAbsolutePath().toString());
        windupCfg.setSourceMode(true);

        JavaClassModel classModel1 = context.getFramed().addVertex(null, JavaClassModel.class);
        classModel1.setQualifiedName("com.example.Class1NoToString");
        JavaClassModel classModel2 = context.getFramed().addVertex(null, JavaClassModel.class);
        classModel2.setQualifiedName("com.example.Class2HasToString");

        JavaMethodModel methodModelSomeMethod = context.getFramed().addVertex(null, JavaMethodModel.class);
        methodModelSomeMethod.setJavaClass(classModel2);
        methodModelSomeMethod.setMethodName("foo");
        JavaMethodModel methodModelToString = context.getFramed().addVertex(null, JavaMethodModel.class);
        methodModelToString.setJavaClass(classModel2);
        methodModelToString.setMethodName("toString");

        TestGremlinQueryOnlyRuleProvider provider = new TestGremlinQueryOnlyRuleProvider();
        Configuration configuration = provider.getConfiguration(context);

        RuleSubset.create(configuration).perform(event, evaluationContext);

        List<JavaMethodModel> methodModelList = provider.getResults();
        Assert.assertTrue(methodModelList.size() == 2);
        Assert.assertTrue(methodModelList.get(0) instanceof JavaMethodModel);
        Assert.assertTrue(methodModelList.get(1) instanceof JavaMethodModel);
    }

    // TODO: Create shared method to set up the graph.
    @Test
    public void testSingletonSelection()
    {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        final GraphContext context = factory.create(folder);

        GraphRewrite event = new GraphRewrite(context);
        DefaultEvaluationContext evaluationContext = createEvalContext(event);

        WindupConfigurationModel windupCfg = context.getFramed().addVertex(null, WindupConfigurationModel.class);
        windupCfg.setInputPath(folder.toAbsolutePath().toString());
        windupCfg.setSourceMode(true);

        JavaClassModel classModel1 = context.getFramed().addVertex(null, JavaClassModel.class);
        classModel1.setQualifiedName("com.example.Class1NoToString");
        JavaClassModel classModel2 = context.getFramed().addVertex(null, JavaClassModel.class);
        classModel2.setQualifiedName("com.example.Class2HasToString");

        JavaMethodModel methodModelSomeMethod = context.getFramed().addVertex(null, JavaMethodModel.class);
        methodModelSomeMethod.setJavaClass(classModel2);
        methodModelSomeMethod.setMethodName("foo");
        JavaMethodModel methodModelToString = context.getFramed().addVertex(null, JavaMethodModel.class);
        methodModelToString.setJavaClass(classModel2);
        methodModelToString.setMethodName("toString");

        TestWindupConfigurationExampleRuleProvider provider = new TestWindupConfigurationExampleRuleProvider();
        Configuration configuration = provider.getConfiguration(context);

        RuleSubset.create(configuration).perform(event, evaluationContext);

        List<JavaMethodModel> methodModelList = provider.getResults();
        Assert.assertTrue(methodModelList.size() == 1);
        Assert.assertNotNull(methodModelList.get(0));
        Assert.assertNotNull(methodModelList.get(0).getJavaClass());
        Assert.assertEquals("toString", methodModelList.get(0).getMethodName());
        Assert.assertEquals(classModel2.getQualifiedName(), methodModelList.get(0).getJavaClass().getQualifiedName());

        WindupConfigurationModel foundCfgModel = provider.getConfig();
        Assert.assertNotNull(foundCfgModel);
        Assert.assertEquals(windupCfg.getInputPath(), foundCfgModel.getInputPath());
    }

    @Test
    public void testJavaMethodModel()
    {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        final GraphContext context = factory.create(folder);

        GraphRewrite event = new GraphRewrite(context);
        DefaultEvaluationContext evaluationContext = createEvalContext(event);

        JavaClassModel classModel1 = context.getFramed().addVertex(null, JavaClassModel.class);
        classModel1.setQualifiedName("com.example.Class1NoToString");
        JavaClassModel classModel2 = context.getFramed().addVertex(null, JavaClassModel.class);
        classModel2.setQualifiedName("com.example.Class2HasToString");

        JavaMethodModel methodModelSomeMethod = context.getFramed().addVertex(null, JavaMethodModel.class);
        methodModelSomeMethod.setJavaClass(classModel2);
        methodModelSomeMethod.setMethodName("foo");
        JavaMethodModel methodModelToString = context.getFramed().addVertex(null, JavaMethodModel.class);
        methodModelToString.setJavaClass(classModel2);
        methodModelToString.setMethodName("toString");

        TestJavaExampleRuleProvider provider = new TestJavaExampleRuleProvider();
        Configuration configuration = provider.getConfiguration(context);

        RuleSubset.create(configuration).perform(event, evaluationContext);

        List<JavaMethodModel> methodModelList = provider.getResults();
        Assert.assertTrue(methodModelList.size() == 1);
        Assert.assertNotNull(methodModelList.get(0));
        Assert.assertNotNull(methodModelList.get(0).getJavaClass());
        Assert.assertEquals("toString", methodModelList.get(0).getMethodName());
        Assert.assertEquals(classModel2.getQualifiedName(), methodModelList.get(0).getJavaClass().getQualifiedName());
    }

    @Test
    public void testTypeTransition()
    {
        // build the initial graph
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        final GraphContext context = factory.create(folder);

        fillData(context);
        context.getGraph().getBaseGraph().commit();

        // setup the context for the rules
        GraphRewrite event = new GraphRewrite(context);
        DefaultEvaluationContext evaluationContext = createEvalContext(event);

        // build a configuration, and make sure it matches what we expect (4 items)
        TestMavenExampleRuleProvider provider = new TestMavenExampleRuleProvider();
        Configuration configuration = provider.getConfiguration(context);
        RuleSubset.create(configuration).perform(event, evaluationContext);

        Assert.assertEquals(4, provider.getSearchResults().size());
    }

    @Test
    public void testTypeFilter()
    {
        // build the initial graph
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        final GraphContext context = factory.create(folder);

        fillData(context);
        context.getGraph().getBaseGraph().commit();

        // setup the context for the rules
        GraphRewrite event = new GraphRewrite(context);
        DefaultEvaluationContext evaluationContext = createEvalContext(event);

        // build a configuration, and make sure it matches what we expect (4 items)
        TestXmlExampleRuleProvider1 provider = new TestXmlExampleRuleProvider1();
        Configuration configuration = provider.getConfiguration(context);
        RuleSubset.create(configuration).perform(event, evaluationContext);

        Assert.assertEquals(4, provider.getTypeSearchResults().size());
        Assert.assertEquals(3, provider.getXmlRootNames().size());
        Assert.assertTrue(provider.getXmlRootNames().contains("xmlTag1"));
        Assert.assertTrue(provider.getXmlRootNames().contains("xmlTag2"));
        Assert.assertFalse(provider.getXmlRootNames().contains("xmlTag3"));
        Assert.assertTrue(provider.getXmlRootNames().contains("xmlTag4"));
        Assert.assertFalse(provider.getXmlRootNames().contains("xmlTag5"));
        Assert.assertEquals(1, provider.getExcludedXmlRootNames().size());
        Assert.assertTrue(provider.getExcludedXmlRootNames().contains("xmlTag3"));
    }

    @Test
    public void testPropertyFilter()
    {
        // build the initial graph
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        final GraphContext context = factory.create(folder);

        fillData(context);
        context.getGraph().getBaseGraph().commit();

        // setup the context for the rules
        GraphRewrite event = new GraphRewrite(context);
        DefaultEvaluationContext evaluationContext = createEvalContext(event);

        // build a configuration, and make sure it matches what we expect (4 items)
        TestXmlExampleRuleProvider2 provider = new TestXmlExampleRuleProvider2();
        Configuration configuration = provider.getConfiguration(context);
        RuleSubset.create(configuration).perform(event, evaluationContext);

        Assert.assertEquals(1, provider.getTypeSearchResults().size());
        Assert.assertEquals("xmlTag3", provider.getTypeSearchResults().get(0).getRootTagName());
    }

    @Test
    public void testTypeAndPropertyFilter()
    {
        // build the initial graph
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        final GraphContext context = factory.create(folder);

        fillData(context);
        context.getGraph().getBaseGraph().commit();

        // setup the context for the rules
        GraphRewrite event = new GraphRewrite(context);
        DefaultEvaluationContext evaluationContext = createEvalContext(event);

        // build a configuration, and make sure it matches what we expect (4 items)
        TestXmlExampleRuleProvider3 provider = new TestXmlExampleRuleProvider3();
        Configuration configuration = provider.getConfiguration(context);
        RuleSubset.create(configuration).perform(event, evaluationContext);

        Assert.assertEquals(1, provider.getTypeSearchResults().size());
        TestXmlMetaFacetModel result1 = provider.getTypeSearchResults().get(0);
        Assert.assertEquals("xmlTag2", result1.getRootTagName());
    }
}
