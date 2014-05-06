package org.jboss.windup.graph.typedgraph;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.dao.BaseDao;
import org.jboss.windup.graph.dao.XmlResourceDao;
import org.jboss.windup.graph.model.meta.BaseMetaModel;
import org.jboss.windup.graph.model.meta.WindupVertexFrame;
import org.jboss.windup.graph.model.meta.xml.XmlMetaFacetModel;
import org.jboss.windup.graph.model.resource.XmlResourceModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@RunWith(Arquillian.class)
public class GraphTypeManagerTest
{
    @Deployment
    @Dependencies({
             @AddonDependency(name = "org.jboss.windup.addon:graph"),
             @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
       ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
                .addBeansXML()
                .addAsAddonDependencies(
                         AddonDependencyEntry.create("org.jboss.windup.addon:graph"),
                         AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
                );
       return archive;
    }

    @Inject
    private GraphContext context;
    @Inject
    private XmlResourceDao xmlResourceDao;

    @Test
    public void testGraphTypeHandling() throws Exception
    {
       Assert.assertNotNull(context);
       
       // First, create a base object
       BaseMetaModel baseMetaModel = context.getFramed().addVertex(null, BaseMetaModel.class);
       
       // Now cast it to an xml object
       xmlResourceDao.castToType(baseMetaModel);
       
       // Now reload it as a base meta object (this returns an iterable, but there should only be one result)
       Iterable<Vertex> vertices = context.getFramed().query().has("type", Text.CONTAINS, BaseMetaModel.class.getAnnotation(TypeValue.class).value()).vertices();
       for (Vertex v : vertices) {
           WindupVertexFrame framed = context.getFramed().frame(v, WindupVertexFrame.class);
           
           // because the type information is stored in the Vertex, this should include at least the following types:
           //  - BaseMetaModel
           //  - XmlResourceModel
           Assert.assertTrue(framed instanceof BaseMetaModel);
           Assert.assertTrue(framed instanceof XmlResourceModel);
       }
    }
}
