package org.jboss.windup.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.model.TestSomeModel;
import org.jboss.windup.config.model.TestXmlMetaFacetModel;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryBuilderFind;
import org.jboss.windup.config.query.QueryBuilderFrom;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaMethodModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

import com.google.common.collect.Iterables;

@RunWith(Arquillian.class)
public class QueryConditionTest {
    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        final AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClasses(TestMavenExampleRuleProvider.class,
                        TestJavaExampleRuleProvider.class,
                        TestXmlExampleRuleProvider1.class,
                        TestXmlExampleRuleProvider2.class,
                        TestXmlExampleRuleProvider3.class,
                        TestGremlinQueryOnlyRuleProvider.class,
                        TestXmlMetaFacetModel.class,
                        TestSomeModel.class,
                        TestWindupConfigurationExampleRuleProvider.class);
        return archive;
    }

    @Inject
    private GraphContextFactory factory;

    private DefaultEvaluationContext createEvalContext(GraphRewrite event) {
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);
        return evaluationContext;
    }

    private void fillData(final GraphContext context) {
        context.getFramed().addFramedVertex(TestSomeModel.class);
        context.getFramed().addFramedVertex(TestSomeModel.class);
        context.getFramed().addFramedVertex(TestSomeModel.class);
        context.getFramed().addFramedVertex(TestSomeModel.class);

        TestXmlMetaFacetModel xmlFacet1 = context.getFramed().addFramedVertex(TestXmlMetaFacetModel.class);
        xmlFacet1.setRootTagName("xmlTag1");
        TestXmlMetaFacetModel xmlFacet2 = context.getFramed().addFramedVertex(TestXmlMetaFacetModel.class);
        xmlFacet2.setRootTagName("xmlTag2");
        TestXmlMetaFacetModel xmlFacet3 = context.getFramed().addFramedVertex(TestXmlMetaFacetModel.class);
        xmlFacet3.setRootTagName("xmlTag3");
        TestXmlMetaFacetModel xmlFacet4 = context.getFramed().addFramedVertex(TestXmlMetaFacetModel.class);
        xmlFacet4.setRootTagName("xmlTag4");
    }

    @Test
    public void testInitialQueryAsGremlin() throws Exception {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder, true)) {
            GraphRewrite event = new GraphRewrite(context);
            DefaultEvaluationContext evaluationContext = createEvalContext(event);

            WindupConfigurationModel windupCfg = context.getFramed().addFramedVertex(WindupConfigurationModel.class);
            FileService fileModelService = new FileService(context);
            windupCfg.addInputPath(fileModelService.createByFilePath(folder.toAbsolutePath().toString()));

            JavaClassModel classModel1 = context.getFramed().addFramedVertex(JavaClassModel.class);
            classModel1.setQualifiedName("com.example.Class1NoToString");
            JavaClassModel classModel2 = context.getFramed().addFramedVertex(JavaClassModel.class);
            classModel2.setQualifiedName("com.example.Class2HasToString");

            JavaMethodModel methodModelSomeMethod = context.getFramed().addFramedVertex(JavaMethodModel.class);
            methodModelSomeMethod.setJavaClass(classModel2);
            methodModelSomeMethod.setMethodName("foo");
            JavaMethodModel methodModelToString = context.getFramed().addFramedVertex(JavaMethodModel.class);
            methodModelToString.setJavaClass(classModel2);
            methodModelToString.setMethodName("toString");

            TestGremlinQueryOnlyRuleProvider provider = new TestGremlinQueryOnlyRuleProvider();
            Configuration configuration = provider.getConfiguration(null);

            RuleSubset.create(configuration).perform(event, evaluationContext);

            List<JavaMethodModel> methodModelList = provider.getResults();
            Assert.assertTrue(methodModelList.size() == 2);
            Assert.assertTrue(methodModelList.get(0) instanceof JavaMethodModel);
            Assert.assertTrue(methodModelList.get(1) instanceof JavaMethodModel);
        }
    }

    // TODO: Create shared method to set up the graph.
    @Test
    public void testSingletonSelection() throws Exception {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder, true)) {

            GraphRewrite event = new GraphRewrite(context);
            DefaultEvaluationContext evaluationContext = createEvalContext(event);

            WindupConfigurationModel windupCfg = context.getFramed().addFramedVertex(WindupConfigurationModel.class);
            FileService fileModelService = new FileService(context);
            windupCfg.addInputPath(fileModelService.createByFilePath(folder.toAbsolutePath().toString()));

            JavaClassModel classModel1 = context.getFramed().addFramedVertex(JavaClassModel.class);
            classModel1.setQualifiedName("com.example.Class1NoToString");
            JavaClassModel classModel2 = context.getFramed().addFramedVertex(JavaClassModel.class);
            classModel2.setQualifiedName("com.example.Class2HasToString");

            JavaMethodModel methodModelSomeMethod = context.getFramed().addFramedVertex(JavaMethodModel.class);
            methodModelSomeMethod.setJavaClass(classModel2);
            methodModelSomeMethod.setMethodName("foo");
            JavaMethodModel methodModelToString = context.getFramed().addFramedVertex(JavaMethodModel.class);
            methodModelToString.setJavaClass(classModel2);
            methodModelToString.setMethodName("toString");

            TestWindupConfigurationExampleRuleProvider provider = new TestWindupConfigurationExampleRuleProvider();
            Configuration configuration = provider.getConfiguration(null);

            RuleSubset.create(configuration).perform(event, evaluationContext);

            List<JavaMethodModel> methodModelList = provider.getResults();
            Assert.assertTrue(methodModelList.size() == 1);
            Assert.assertNotNull(methodModelList.get(0));
            Assert.assertNotNull(methodModelList.get(0).getJavaClass());
            Assert.assertEquals("toString", methodModelList.get(0).getMethodName());
            Assert.assertEquals(classModel2.getQualifiedName(), methodModelList.get(0).getJavaClass()
                    .getQualifiedName());

            WindupConfigurationModel foundCfgModel = provider.getConfig();
            Assert.assertNotNull(foundCfgModel);

            List<FileModel> originalInputPaths = new ArrayList<>();
            Iterables.addAll(originalInputPaths, windupCfg.getInputPaths());

            List<FileModel> queriedInputPaths = new ArrayList<>();
            Iterables.addAll(queriedInputPaths, foundCfgModel.getInputPaths());

            Assert.assertEquals(originalInputPaths, queriedInputPaths);
        }
    }

    @Test
    public void testJavaMethodModel() throws Exception {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder, true)) {

            GraphRewrite event = new GraphRewrite(context);
            DefaultEvaluationContext evaluationContext = createEvalContext(event);

            JavaClassModel classModel1 = context.getFramed().addFramedVertex(JavaClassModel.class);
            classModel1.setQualifiedName("com.example.Class1NoToString");
            JavaClassModel classModel2 = context.getFramed().addFramedVertex(JavaClassModel.class);
            classModel2.setQualifiedName("com.example.Class2HasToString");

            JavaMethodModel methodModelSomeMethod = context.getFramed().addFramedVertex(JavaMethodModel.class);
            methodModelSomeMethod.setJavaClass(classModel2);
            methodModelSomeMethod.setMethodName("foo");
            JavaMethodModel methodModelToString = context.getFramed().addFramedVertex(JavaMethodModel.class);
            methodModelToString.setJavaClass(classModel2);
            methodModelToString.setMethodName("toString");

            TestJavaExampleRuleProvider provider = new TestJavaExampleRuleProvider();
            Configuration configuration = provider.getConfiguration(null);

            RuleSubset.create(configuration).perform(event, evaluationContext);

            List<JavaMethodModel> methodModelList = provider.getResults();
            Assert.assertTrue(methodModelList.size() == 1);
            Assert.assertNotNull(methodModelList.get(0));
            Assert.assertNotNull(methodModelList.get(0).getJavaClass());
            Assert.assertEquals("toString", methodModelList.get(0).getMethodName());
            Assert.assertEquals(classModel2.getQualifiedName(), methodModelList.get(0).getJavaClass()
                    .getQualifiedName());
        }
    }

    @Test
    public void testTypeTransition() throws Exception {
        // build the initial graph
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder, true)) {

            fillData(context);
            context.commit();

            // setup the context for the rules
            GraphRewrite event = new GraphRewrite(context);
            DefaultEvaluationContext evaluationContext = createEvalContext(event);

            // build a configuration, and make sure it matches what we expect (4 items)
            TestMavenExampleRuleProvider provider = new TestMavenExampleRuleProvider();
            Configuration configuration = provider.getConfiguration(null);
            RuleSubset.create(configuration).perform(event, evaluationContext);

            Assert.assertEquals(4, provider.getSearchResults().size());
        }
    }

    @Test
    public void testTypeFilter() throws Exception {
        // build the initial graph
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder, true)) {

            fillData(context);
            context.commit();

            // setup the context for the rules
            GraphRewrite event = new GraphRewrite(context);
            DefaultEvaluationContext evaluationContext = createEvalContext(event);

            // build a configuration, and make sure it matches what we expect (4 items)
            TestXmlExampleRuleProvider1 provider = new TestXmlExampleRuleProvider1();
            Configuration configuration = provider.getConfiguration(null);
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
    }

    @Test
    public void testPropertyFilter() throws Exception {
        // build the initial graph
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder, true)) {

            fillData(context);
            context.commit();

            // setup the context for the rules
            GraphRewrite event = new GraphRewrite(context);
            DefaultEvaluationContext evaluationContext = createEvalContext(event);

            // build a configuration, and make sure it matches what we expect (4 items)
            TestXmlExampleRuleProvider2 provider = new TestXmlExampleRuleProvider2();
            Configuration configuration = provider.getConfiguration(null);
            RuleSubset.create(configuration).perform(event, evaluationContext);

            Assert.assertEquals(1, provider.getTypeSearchResults().size());
            Assert.assertEquals("xmlTag3", provider.getTypeSearchResults().get(0).getRootTagName());
        }
    }

    @Test
    public void testTypeAndPropertyFilter() throws Exception {
        // build the initial graph
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder, true)) {
            fillData(context);
            context.commit();

            // setup the context for the rules
            GraphRewrite event = new GraphRewrite(context);
            DefaultEvaluationContext evaluationContext = createEvalContext(event);

            // build a configuration, and make sure it matches what we expect (4 items)
            TestXmlExampleRuleProvider3 provider = new TestXmlExampleRuleProvider3();
            Configuration configuration = provider.getConfiguration(null);
            RuleSubset.create(configuration).perform(event, evaluationContext);

            Assert.assertEquals(1, provider.getTypeSearchResults().size());
            TestXmlMetaFacetModel result1 = provider.getTypeSearchResults().get(0);
            Assert.assertEquals("xmlTag2", result1.getRootTagName());
        }
    }

    @Test
    public void testExcludeTypeFilter() throws Exception {
        try (final GraphContext context = factory.create(true)) {
            GraphRewrite event = new GraphRewrite(context);
            DefaultEvaluationContext evaluationContext = createEvalContext(event);

            fillData(context);

            WindupVertexFrame bothTypesFrame = context.getFramed().addFramedVertex(TestSomeModel.class);
            bothTypesFrame = GraphService.addTypeToModel(context, bothTypesFrame, TestXmlMetaFacetModel.class);

            context.commit();

            Variables variables = Variables.instance(event);
            variables.push();
            QueryBuilderFrom q = Query.fromType(TestSomeModel.class);
            q.as("allResults");
            boolean resultsFound = q.evaluate(event, evaluationContext);
            Assert.assertTrue(resultsFound);

            Iterable<? extends WindupVertexFrame> allResults = variables.findVariable("allResults");
            Assert.assertEquals(5, Iterables.size(allResults));

            variables.pop();

            variables.push();
            QueryBuilderFind qExcluded = Query.fromType(TestSomeModel.class).excludingType(TestXmlMetaFacetModel.class);
            qExcluded.as("withTypeExcluded");
            boolean excludedResultsFound = qExcluded.evaluate(event, evaluationContext);
            Assert.assertTrue(excludedResultsFound);
            Iterable<? extends WindupVertexFrame> excludedResults = variables.findVariable("withTypeExcluded");
            Assert.assertEquals(4, Iterables.size(excludedResults));

            for (WindupVertexFrame frame : excludedResults) {
                Assert.assertTrue(frame instanceof TestSomeModel);
                Assert.assertFalse(frame instanceof TestXmlMetaFacetModel);
            }

            variables.pop();
        }
    }

    @Test
    public void testIncludeTypeFilter() throws Exception {
        try (final GraphContext context = factory.create(true)) {
            GraphRewrite event = new GraphRewrite(context);
            DefaultEvaluationContext evaluationContext = createEvalContext(event);

            fillData(context);

            WindupVertexFrame bothTypesFrame = context.getFramed().addFramedVertex(TestSomeModel.class);
            bothTypesFrame = GraphService.addTypeToModel(context, bothTypesFrame, TestXmlMetaFacetModel.class);

            context.commit();

            Variables variables = Variables.instance(event);
            variables.push();
            QueryBuilderFrom q = Query.fromType(TestSomeModel.class);
            q.as("allResults");
            boolean resultsFound = q.evaluate(event, evaluationContext);
            Assert.assertTrue(resultsFound);

            Iterable<? extends WindupVertexFrame> allResults = variables.findVariable("allResults");
            Assert.assertEquals(5, Iterables.size(allResults));

            variables.pop();

            variables.push();
            QueryBuilderFind queryIncluded = Query.fromType(TestSomeModel.class).includingType(TestXmlMetaFacetModel.class);
            queryIncluded.as("withTypeIncluded");
            boolean excludedResultsFound = queryIncluded.evaluate(event, evaluationContext);
            Assert.assertTrue(excludedResultsFound);
            Iterable<? extends WindupVertexFrame> includedResults = variables.findVariable("withTypeIncluded");
            Assert.assertEquals(1, Iterables.size(includedResults));

            for (WindupVertexFrame frame : includedResults) {
                Assert.assertTrue(frame instanceof TestSomeModel);
                Assert.assertTrue(frame instanceof TestXmlMetaFacetModel);
            }

            variables.pop();
        }
    }
}
