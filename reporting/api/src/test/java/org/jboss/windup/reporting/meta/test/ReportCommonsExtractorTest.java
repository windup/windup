package org.jboss.windup.reporting.meta.test;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.reporting.util.ReportCommonsExtractor;
import org.jboss.windup.reporting.meta.ReportableInfo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


@RunWith(Arquillian.class)
public class ReportCommonsExtractorTest
{
    @Deployment
    @Dependencies({
        @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
        @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class)
            .addBeansXML()
            .addClasses(ReportCommonsTestModel.class, ReportCommonsTestSubModel.class)
            .addAsAddonDependencies(
                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi")
            );
        return archive;
    }

    @Inject
    private GraphContext context;


    @Test
    public void testExtract_Class() {
        System.out.println( "extract" );
        Class<? extends WindupVertexFrame> modelClass = ReportCommonsTestModel.class;
        ReportCommonsExtractor instance = null;
        ReportableInfo expResult = null;
        ReportableInfo result = instance.extract( modelClass );
        assertEquals( expResult, result );
        fail( "The test case is a prototype." );
    }


    @Test
    public void testExtract_WindupVertexFrame() {
        System.out.println( "extract" );
        WindupVertexFrame frame = null;
        ReportCommonsExtractor instance = null;
        ReportableInfo expResult = null;
        ReportableInfo result = instance.extract( frame );
        assertEquals( expResult, result );
        fail( "The test case is a prototype." );
    }


    @Test
    public void testExtract_WindupVertexFrame_ReportableInfo() {
        System.out.println( "extract" );
        WindupVertexFrame frame = null;
        ReportableInfo ri = null;
        ReportCommonsExtractor instance = null;
        ReportableInfo expResult = null;
        ReportableInfo result = instance.extract( frame, ri );
        assertEquals( expResult, result );
        fail( "The test case is a prototype." );
    }
    

    @Test
    public void testGraphTypeHandling() throws Exception
    {

        ReportCommonsTestModel initialModelType = context.getFramed().addVertex(null, ReportCommonsTestModel.class);

        GraphService.addTypeToModel(context, initialModelType, ReportCommonsTestSubModel.class);

        Iterable<Vertex> vertices = context.getFramed().query()
                    .has("type", Text.CONTAINS, ReportCommonsTestModel.class.getAnnotation(TypeValue.class).value())
                    .vertices();

        int numberFound = 0;
        for (Vertex v : vertices)
        {
            numberFound++;
            WindupVertexFrame framed = context.getFramed().frame(v, WindupVertexFrame.class);

            Assert.assertTrue(framed instanceof ReportCommonsTestModel);
            Assert.assertTrue(framed instanceof ReportCommonsTestSubModel);
        }
        Assert.assertEquals(1, numberFound);
    }
}
