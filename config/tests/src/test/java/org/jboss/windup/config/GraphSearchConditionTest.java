package org.jboss.windup.config;


import java.io.File;
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
import org.jboss.windup.config.runner.DefaultEvaluationContext;
import org.jboss.windup.config.selectables.SelectionFactory;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextImpl;
import org.jboss.windup.graph.model.meta.xml.WebConfigurationFacetModel;
import org.jboss.windup.graph.model.meta.xml.XmlMetaFacetModel;
import org.jboss.windup.graph.typedgraph.GraphTypeRegistry;
import org.jboss.windup.rules.apps.java.scan.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.scan.model.JavaMethodModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

@RunWith(Arquillian.class)
public class GraphSearchConditionTest
{
    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addClasses(MavenExampleConfigurationProvider.class,
                                JavaExampleConfigurationProvider.class,
                                XmlExampleConfigurationProvider1.class,
                                XmlExampleConfigurationProvider2.class,
                                XmlExampleConfigurationProvider3.class)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.config:windup-config"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private GraphTypeRegistry graphTypeRegistry;

    @Inject
    private SelectionFactory selectionFactory;

    @Test
    public void testJavaMethodModel()
    {
        final File folder = OperatingSystemUtils.createTempDir();
        final GraphContext context = new GraphContextImpl(folder, graphTypeRegistry);
        GraphRewrite event = new GraphRewrite(context);
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);
        event.getRewriteContext().put(SelectionFactory.class, selectionFactory);

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

        JavaExampleConfigurationProvider provider = new JavaExampleConfigurationProvider();
        Configuration configuration = provider.getConfiguration(context);

        GraphSubset.evaluate(configuration).perform(event, evaluationContext);

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
        final File folder = OperatingSystemUtils.createTempDir();
        final GraphContext context = new GraphContextImpl(folder, graphTypeRegistry);

        context.getFramed().addVertex(null, WebConfigurationFacetModel.class);
        context.getFramed().addVertex(null, WebConfigurationFacetModel.class);
        context.getFramed().addVertex(null, WebConfigurationFacetModel.class);
        context.getFramed().addVertex(null, WebConfigurationFacetModel.class);

        XmlMetaFacetModel xmlFacet1 = context.getFramed().addVertex(null, XmlMetaFacetModel.class);
        xmlFacet1.setRootTagName("xmlTag1");
        XmlMetaFacetModel xmlFacet2 = context.getFramed().addVertex(null, XmlMetaFacetModel.class);
        xmlFacet2.setRootTagName("xmlTag2");
        XmlMetaFacetModel xmlFacet3 = context.getFramed().addVertex(null, XmlMetaFacetModel.class);
        xmlFacet3.setRootTagName("xmlTag3");
        XmlMetaFacetModel xmlFacet4 = context.getFramed().addVertex(null, XmlMetaFacetModel.class);
        xmlFacet4.setRootTagName("xmlTag4");
        context.getGraph().commit();

        // setup the context for the rules
        GraphRewrite event = new GraphRewrite(context);
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);
        event.getRewriteContext().put(SelectionFactory.class, selectionFactory);

        // build a configuration, and make sure it matches what we expect (4 items)
        MavenExampleConfigurationProvider provider = new MavenExampleConfigurationProvider();
        Configuration configuration = provider.getConfiguration(context);
        GraphSubset.evaluate(configuration).perform(event, evaluationContext);

        Assert.assertEquals(4, provider.getSearchResults().size());
    }

    @Test
    public void testTypeFilter()
    {
        // build the initial graph
        final File folder = OperatingSystemUtils.createTempDir();
        final GraphContext context = new GraphContextImpl(folder, graphTypeRegistry);

        context.getFramed().addVertex(null, WebConfigurationFacetModel.class);
        context.getFramed().addVertex(null, WebConfigurationFacetModel.class);
        context.getFramed().addVertex(null, WebConfigurationFacetModel.class);
        context.getFramed().addVertex(null, WebConfigurationFacetModel.class);

        XmlMetaFacetModel xmlFacet1 = context.getFramed().addVertex(null, XmlMetaFacetModel.class);
        xmlFacet1.setRootTagName("xmlTag1");
        XmlMetaFacetModel xmlFacet2 = context.getFramed().addVertex(null, XmlMetaFacetModel.class);
        xmlFacet2.setRootTagName("xmlTag2");
        XmlMetaFacetModel xmlFacet3 = context.getFramed().addVertex(null, XmlMetaFacetModel.class);
        xmlFacet3.setRootTagName("xmlTag3");
        XmlMetaFacetModel xmlFacet4 = context.getFramed().addVertex(null, XmlMetaFacetModel.class);
        xmlFacet4.setRootTagName("xmlTag4");
        context.getGraph().commit();

        // setup the context for the rules
        GraphRewrite event = new GraphRewrite(context);
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);
        event.getRewriteContext().put(SelectionFactory.class, selectionFactory);

        // build a configuration, and make sure it matches what we expect (4 items)
        XmlExampleConfigurationProvider1 provider = new XmlExampleConfigurationProvider1();
        Configuration configuration = provider.getConfiguration(context);
        GraphSubset.evaluate(configuration).perform(event, evaluationContext);

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
        final File folder = OperatingSystemUtils.createTempDir();
        final GraphContext context = new GraphContextImpl(folder, graphTypeRegistry);

        context.getFramed().addVertex(null, WebConfigurationFacetModel.class);
        context.getFramed().addVertex(null, WebConfigurationFacetModel.class);
        context.getFramed().addVertex(null, WebConfigurationFacetModel.class);
        context.getFramed().addVertex(null, WebConfigurationFacetModel.class);

        XmlMetaFacetModel xmlFacet1 = context.getFramed().addVertex(null, XmlMetaFacetModel.class);
        xmlFacet1.setRootTagName("xmlTag1");
        XmlMetaFacetModel xmlFacet2 = context.getFramed().addVertex(null, XmlMetaFacetModel.class);
        xmlFacet2.setRootTagName("xmlTag2");
        XmlMetaFacetModel xmlFacet3 = context.getFramed().addVertex(null, XmlMetaFacetModel.class);
        xmlFacet3.setRootTagName("xmlTag3");
        XmlMetaFacetModel xmlFacet4 = context.getFramed().addVertex(null, XmlMetaFacetModel.class);
        xmlFacet4.setRootTagName("xmlTag4");
        context.getGraph().commit();

        // setup the context for the rules
        GraphRewrite event = new GraphRewrite(context);
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);
        event.getRewriteContext().put(SelectionFactory.class, selectionFactory);

        // build a configuration, and make sure it matches what we expect (4 items)
        XmlExampleConfigurationProvider2 provider = new XmlExampleConfigurationProvider2();
        Configuration configuration = provider.getConfiguration(context);
        GraphSubset.evaluate(configuration).perform(event, evaluationContext);

        Assert.assertEquals(1, provider.getTypeSearchResults().size());
        Assert.assertEquals("xmlTag3", provider.getTypeSearchResults().get(0).getRootTagName());
    }

    @Test
    public void testTypeAndPropertyFilter()
    {
        // build the initial graph
        final File folder = OperatingSystemUtils.createTempDir();
        final GraphContext context = new GraphContextImpl(folder, graphTypeRegistry);

        context.getFramed().addVertex(null, WebConfigurationFacetModel.class);
        context.getFramed().addVertex(null, WebConfigurationFacetModel.class);
        context.getFramed().addVertex(null, WebConfigurationFacetModel.class);
        context.getFramed().addVertex(null, WebConfigurationFacetModel.class);

        XmlMetaFacetModel xmlFacet1 = context.getFramed().addVertex(null, XmlMetaFacetModel.class);
        xmlFacet1.setRootTagName("xmlTag1");
        XmlMetaFacetModel xmlFacet2 = context.getFramed().addVertex(null, XmlMetaFacetModel.class);
        xmlFacet2.setRootTagName("xmlTag2");
        XmlMetaFacetModel xmlFacet3 = context.getFramed().addVertex(null, XmlMetaFacetModel.class);
        xmlFacet3.setRootTagName("xmlTag3");
        XmlMetaFacetModel xmlFacet4 = context.getFramed().addVertex(null, XmlMetaFacetModel.class);
        xmlFacet4.setRootTagName("xmlTag4");
        context.getGraph().commit();

        // setup the context for the rules
        GraphRewrite event = new GraphRewrite(context);
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);
        event.getRewriteContext().put(SelectionFactory.class, selectionFactory);

        // build a configuration, and make sure it matches what we expect (4 items)
        XmlExampleConfigurationProvider3 provider = new XmlExampleConfigurationProvider3();
        Configuration configuration = provider.getConfiguration(context);
        GraphSubset.evaluate(configuration).perform(event, evaluationContext);

        Assert.assertEquals(1, provider.getTypeSearchResults().size());
        XmlMetaFacetModel result1 = provider.getTypeSearchResults().get(0);
        Assert.assertEquals("xmlTag2", result1.getRootTagName());
    }
}
