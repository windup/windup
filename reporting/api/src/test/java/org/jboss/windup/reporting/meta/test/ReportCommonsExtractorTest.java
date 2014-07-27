package org.jboss.windup.reporting.meta.test;

import org.jboss.windup.reporting.meta.test.model.ReportCommonsTestElementSubModel;
import org.jboss.windup.reporting.meta.test.model.ReportCommonsTestElementModel;
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
import org.jboss.windup.reporting.meta.ReportCommons;
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
            .addClasses(ReportCommonsTestElementModel.class, ReportCommonsTestElementSubModel.class)
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
        Class<? extends WindupVertexFrame> modelClass = ReportCommonsTestElementModel.class;
        ReportCommons rc = ReportCommonsExtractor.extract( modelClass );
        assertEquals("Report Commons Test ${this.name}", rc.getTitle() );
    }


    @Test
    public void testExtract_WindupVertexFrame() {
        System.out.println( "extract" );
        ReportCommonsTestElementModel frame = new GraphService<ReportCommonsTestElementModel>(context, ReportCommonsTestElementModel.class).create();
        frame.setName("Hanka");
                
        ReportCommonsExtractor instance = new ReportCommonsExtractor(null);
        ReportCommons rc = instance.extract( frame );
        assertEquals("Report Commons Test Hanka", rc.getTitle() );
    }

    
    @Test
    public void testGraphTypeHandling() throws Exception
    {

        ReportCommonsTestElementModel initialModelType = context.getFramed().addVertex(null, ReportCommonsTestElementModel.class);

        GraphService.addTypeToModel(context, initialModelType, ReportCommonsTestElementSubModel.class);

        Iterable<Vertex> vertices = context.getFramed().query()
                    .has("type", Text.CONTAINS, ReportCommonsTestElementModel.class.getAnnotation(TypeValue.class).value())
                    .vertices();

        int numberFound = 0;
        for (Vertex v : vertices)
        {
            numberFound++;
            WindupVertexFrame framed = context.getFramed().frame(v, WindupVertexFrame.class);

            Assert.assertTrue(framed instanceof ReportCommonsTestElementModel);
            Assert.assertTrue(framed instanceof ReportCommonsTestElementSubModel);
        }
        Assert.assertEquals(1, numberFound);
    }
}
