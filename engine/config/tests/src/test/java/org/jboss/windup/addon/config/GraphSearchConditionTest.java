package org.jboss.windup.addon.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.addon.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.addon.config.graphsearch.GraphSearchPropertyComparisonType;
import org.jboss.windup.addon.config.operation.GraphOperation;
import org.jboss.windup.addon.config.operation.Iteration;
import org.jboss.windup.addon.config.runner.DefaultEvaluationContext;
import org.jboss.windup.addon.config.selectables.SelectionFactory;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextImpl;
import org.jboss.windup.graph.model.meta.xml.WebConfigurationFacetModel;
import org.jboss.windup.graph.model.meta.xml.XmlMetaFacetModel;
import org.jboss.windup.graph.typedgraph.GraphTypeRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Subset;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

@RunWith(Arquillian.class)
public class GraphSearchConditionTest
{

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.addon:config"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        final ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.addon:config"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                    );
        return archive;
    }

    @Inject
    private GraphTypeRegistry graphTypeRegistry;

    @Inject
    private SelectionFactory selectionFactory;

    @Test
    public void testTypeFilter()
    {
        // build the initial graph
        final File folder = OperatingSystemUtils.createTempDir();
        final GraphContext context = new GraphContextImpl(folder, graphTypeRegistry);

        WebConfigurationFacetModel webCfgFacet1 = context.getFramed().addVertex(null, WebConfigurationFacetModel.class);
        WebConfigurationFacetModel webCfgFacet2 = context.getFramed().addVertex(null, WebConfigurationFacetModel.class);
        WebConfigurationFacetModel webCfgFacet3 = context.getFramed().addVertex(null, WebConfigurationFacetModel.class);
        WebConfigurationFacetModel webCfgFacet4 = context.getFramed().addVertex(null, WebConfigurationFacetModel.class);

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

        final List<XmlMetaFacetModel> typeSearchResults = new ArrayList<>();
        final Set<String> xmlRootNames = new HashSet<>();

        // build a configuration, and make sure it matches what we expect (4 items)
        Configuration configuration = ConfigurationBuilder.begin()
                    .addRule()
                    .when(GraphSearchConditionBuilder.create("xmlModels").has(XmlMetaFacetModel.class))
                    .perform(Iteration.over(XmlMetaFacetModel.class, "xmlModels", "xml")
                                .perform(new GraphOperation()
                                {
                                    @Override
                                    public void perform(GraphRewrite event, EvaluationContext context)
                                    {
                                        SelectionFactory factory = SelectionFactory.instance(event);
                                        XmlMetaFacetModel xmlFacetModel = factory
                                                    .getCurrentPayload(XmlMetaFacetModel.class);
                                        typeSearchResults.add(xmlFacetModel);
                                        if (xmlRootNames.contains(xmlFacetModel.getRootTagName()))
                                        {
                                            Assert.fail("Tag found multiple times");
                                        }
                                        xmlRootNames.add(xmlFacetModel.getRootTagName());
                                    }
                                })
                    );
        Subset.evaluate(configuration).perform(event, evaluationContext);

        Assert.assertTrue(typeSearchResults.size() == 4);
        Assert.assertTrue(xmlRootNames.contains("xmlTag1") && xmlRootNames.contains("xmlTag2")
                    && xmlRootNames.contains("xmlTag3") && xmlRootNames.contains("xmlTag4"));
    }

    @Test
    public void testPropertyFilter()
    {
        // build the initial graph
        final File folder = OperatingSystemUtils.createTempDir();
        final GraphContext context = new GraphContextImpl(folder, graphTypeRegistry);

        WebConfigurationFacetModel webCfgFacet1 = context.getFramed().addVertex(null, WebConfigurationFacetModel.class);
        WebConfigurationFacetModel webCfgFacet2 = context.getFramed().addVertex(null, WebConfigurationFacetModel.class);
        WebConfigurationFacetModel webCfgFacet3 = context.getFramed().addVertex(null, WebConfigurationFacetModel.class);
        WebConfigurationFacetModel webCfgFacet4 = context.getFramed().addVertex(null, WebConfigurationFacetModel.class);

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

        final List<XmlMetaFacetModel> typeSearchResults = new ArrayList<>();

        // build a configuration, and make sure it matches what we expect (4 items)
        Configuration configuration = ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(GraphSearchConditionBuilder
                                .create("xmlModels")
                                .withProperty(XmlMetaFacetModel.PROPERTY_ROOT_TAG_NAME,
                                            GraphSearchPropertyComparisonType.EQUALS,
                                            "xmlTag3"))
                    .perform(Iteration.over(XmlMetaFacetModel.class, "xmlModels", "xml")
                                .perform(new GraphOperation()
                                {
                                    @Override
                                    public void perform(GraphRewrite event, EvaluationContext context)
                                    {
                                        SelectionFactory factory = (SelectionFactory) event.getRewriteContext().get(
                                                    SelectionFactory.class);
                                        XmlMetaFacetModel xmlFacetModel = factory
                                                    .getCurrentPayload(XmlMetaFacetModel.class);
                                        typeSearchResults.add(xmlFacetModel);
                                    }
                                })
                    );
        Subset.evaluate(configuration).perform(event, evaluationContext);

        Assert.assertTrue(typeSearchResults.size() == 1);
        XmlMetaFacetModel result1 = typeSearchResults.get(0);
        Assert.assertEquals("xmlTag3", result1.getRootTagName());
    }

    @Test
    public void testTypeAndPropertyFilter()
    {
        // build the initial graph
        final File folder = OperatingSystemUtils.createTempDir();
        final GraphContext context = new GraphContextImpl(folder, graphTypeRegistry);

        WebConfigurationFacetModel webCfgFacet1 = context.getFramed().addVertex(null, WebConfigurationFacetModel.class);
        WebConfigurationFacetModel webCfgFacet2 = context.getFramed().addVertex(null, WebConfigurationFacetModel.class);
        WebConfigurationFacetModel webCfgFacet3 = context.getFramed().addVertex(null, WebConfigurationFacetModel.class);
        WebConfigurationFacetModel webCfgFacet4 = context.getFramed().addVertex(null, WebConfigurationFacetModel.class);

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

        final List<XmlMetaFacetModel> typeSearchResults = new ArrayList<>();

        // build a configuration, and make sure it matches what we expect (4 items)
        Configuration configuration = ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(GraphSearchConditionBuilder
                                .create("xmlModels")
                                .has(XmlMetaFacetModel.class)
                                .withProperty(XmlMetaFacetModel.PROPERTY_ROOT_TAG_NAME,
                                            GraphSearchPropertyComparisonType.EQUALS,
                                            "xmlTag2"))
                    .perform(Iteration.over(XmlMetaFacetModel.class, "xmlModels", "xml")
                                .perform(new GraphOperation()
                                {
                                    @Override
                                    public void perform(GraphRewrite event, EvaluationContext context)
                                    {
                                        SelectionFactory factory = (SelectionFactory) event.getRewriteContext().get(
                                                    SelectionFactory.class);
                                        XmlMetaFacetModel xmlFacetModel = factory
                                                    .getCurrentPayload(XmlMetaFacetModel.class);
                                        typeSearchResults.add(xmlFacetModel);
                                    }
                                })
                    );
        Subset.evaluate(configuration).perform(event, evaluationContext);

        Assert.assertTrue(typeSearchResults.size() == 1);
        XmlMetaFacetModel result1 = typeSearchResults.get(0);
        Assert.assertEquals("xmlTag2", result1.getRootTagName());
    }
}
