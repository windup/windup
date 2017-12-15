package org.jboss.windup.reporting;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.query.FindSourceReportFilesGremlinCriterion;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.java.query.FindFilesNotClassifiedOrHintedGremlinCriterion;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

@RunWith(Arquillian.class)
public class InlineHintModelQueryTest
{

    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.config:windup-config"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment()
    {
        AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                    .addBeansXML()
                    .addAsResource(new File("src/test/resources/reports"));
        return archive;
    }

    @Inject
    private GraphContextFactory factory;

    private GraphContext context;

    @Before
    public void beforeTest() throws Exception
    {
        context = factory.create();
    }

    @After
    public void afterTest() throws Exception
    {
        context.close();
        context.clear();
    }

    @Test
    public void testFindingClassifiedFiles() throws Exception
    {
        FileModel f1 = context.getFramed().addVertex(null, FileModel.class);
        f1.setFilePath("/f1");
        FileModel f2 = context.getFramed().addVertex(null, FileModel.class);
        f2.setFilePath("/f2");
        FileModel f3 = context.getFramed().addVertex(null, FileModel.class);
        f3.setFilePath("/f3");
        FileModel f4 = context.getFramed().addVertex(null, FileModel.class);
        f4.setFilePath("/f4");
        FileModel f5 = context.getFramed().addVertex(null, FileModel.class);
        f5.setFilePath("/f5");
        FileModel f6 = context.getFramed().addVertex(null, FileModel.class);
        f6.setFilePath("/f6");
        FileModel f7 = context.getFramed().addVertex(null, FileModel.class);
        f7.setFilePath("/f7");

        InlineHintModel b1 = context.getFramed().addVertex(null, InlineHintModel.class);
        InlineHintModel b1b = context.getFramed().addVertex(null, InlineHintModel.class);
        b1.setFile(f1);
        b1b.setFile(f1);

        InlineHintModel b2 = context.getFramed().addVertex(null, InlineHintModel.class);
        b2.setFile(f2);

        ClassificationModel c1 = context.getFramed().addVertex(null, ClassificationModel.class);
        ClassificationModel c1b = context.getFramed().addVertex(null, ClassificationModel.class);
        c1.addFileModel(f1);
        c1b.addFileModel(f1);

        ClassificationModel c2 = context.getFramed().addVertex(null, ClassificationModel.class);
        c2.addFileModel(f3);

        TechnologyTagService techTagService = new TechnologyTagService(context);
        techTagService.addTagToFileModel(f4, "TestTag", TechnologyTagLevel.IMPORTANT);

        List<Vertex> vertexList = new ArrayList<>();
        for (Vertex v : context.getQuery().type(FileModel.class).vertices())
        {
            vertexList.add(v);
        }

        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(context.getQuery().type(FileModel.class)
                    .vertices());

        GraphRewrite event = new GraphRewrite(context);

        // manually execute this criterion (this just adds things to the pipeline)
        new FindSourceReportFilesGremlinCriterion().query(event, pipeline);

        List<FileModel> fileModels = new ArrayList<>();
        for (Vertex v : pipeline)
        {
            // Explicit cast here insures that the frame returned was actually a FileModel. If it is not, a
            // ClassCastException will
            // occur and the test will fail.
            //
            // If we called frame(v, FileModel.class) directly, frames would happily force it to be a FileModel
            // even if the underlying query were returning invalid results.
            FileModel fm = (FileModel) context.getFramed().frame(v, WindupVertexFrame.class);
            fileModels.add(fm);
        }

        boolean foundF1 = false;
        boolean foundF2 = false;
        boolean foundF3 = false;
        boolean foundF4 = false;
        Assert.assertEquals(4, fileModels.size());
        for (FileModel fm : fileModels)
        {
            if (fm.getFilePath().equals(f1.getFilePath()))
            {
                foundF1 = true;
            }
            else if (fm.getFilePath().equals(f2.getFilePath()))
            {
                foundF2 = true;
            }
            else if (fm.getFilePath().equals(f3.getFilePath()))
            {
                foundF3 = true;
            }
            else if (fm.getFilePath().equals(f4.getFilePath()))
            {
                foundF4 = true;
            }
        }
        Assert.assertTrue(foundF1);
        Assert.assertTrue(foundF2);
        Assert.assertTrue(foundF3);
        Assert.assertTrue(foundF4);
    }

    @Test
    public void testFindingNonClassifiedFiles() throws Exception
    {
        FileModel f1 = context.getFramed().addVertex(null, FileModel.class);
        f1.setFilePath("/f1");
        FileModel f2 = context.getFramed().addVertex(null, FileModel.class);
        f2.setFilePath("/f2");
        FileModel f3 = context.getFramed().addVertex(null, FileModel.class);
        f3.setFilePath("/f3");
        FileModel f4 = context.getFramed().addVertex(null, FileModel.class);
        f4.setFilePath("/f4");
        FileModel f5 = context.getFramed().addVertex(null, FileModel.class);
        f5.setFilePath("/f5");
        FileModel f6 = context.getFramed().addVertex(null, FileModel.class);
        f6.setFilePath("/f6");
        FileModel f7 = context.getFramed().addVertex(null, FileModel.class);
        f7.setFilePath("/f7");

        InlineHintModel b1 = context.getFramed().addVertex(null, InlineHintModel.class);
        InlineHintModel b1b = context.getFramed().addVertex(null, InlineHintModel.class);
        b1.setFile(f1);
        b1b.setFile(f1);

        InlineHintModel b2 = context.getFramed().addVertex(null, InlineHintModel.class);
        b2.setFile(f2);

        ClassificationModel c1 = context.getFramed().addVertex(null, ClassificationModel.class);
        ClassificationModel c1b = context.getFramed().addVertex(null, ClassificationModel.class);
        c1.addFileModel(f1);
        c1b.addFileModel(f1);

        ClassificationModel c2 = context.getFramed().addVertex(null, ClassificationModel.class);
        c2.addFileModel(f3);

        List<Vertex> vertexList = new ArrayList<>();
        for (Vertex v : context.getQuery().type(FileModel.class).vertices())
        {
            vertexList.add(v);
        }

        // manually execute this criterion (this just adds things to the pipeline)
        Iterable<Vertex> allFMVertices = context.getQuery().type(FileModel.class).vertices();
        Iterable<Vertex> fileModelIterable = new FindFilesNotClassifiedOrHintedGremlinCriterion()
                    .query(context, allFMVertices);

        List<FileModel> fileModels = new ArrayList<>();
        for (Vertex v : fileModelIterable)
        {
            // Explicit cast here insures that the frame returned was actually a FileModel. If it is not, a
            // ClassCastException will
            // occur and the test will fail.
            //
            // If we called frame(v, FileModel.class) directly, frames would happily force it to be a FileModel
            // even if the underlying query were returning invalid results.
            FileModel fm = (FileModel) context.getFramed().frame(v, WindupVertexFrame.class);
            fileModels.add(fm);
        }

        boolean foundF4 = false;
        boolean foundF5 = false;
        boolean foundF6 = false;
        boolean foundF7 = false;
        Assert.assertEquals(4, fileModels.size());
        for (FileModel fm : fileModels)
        {
            if (fm.getFilePath().equals(f4.getFilePath()))
            {
                foundF4 = true;
            }
            else if (fm.getFilePath().equals(f5.getFilePath()))
            {
                foundF5 = true;
            }
            else if (fm.getFilePath().equals(f6.getFilePath()))
            {
                foundF6 = true;
            }
            else if (fm.getFilePath().equals(f7.getFilePath()))
            {
                foundF7 = true;
            }
        }
        Assert.assertTrue(foundF4);
        Assert.assertTrue(foundF5);
        Assert.assertTrue(foundF6);
        Assert.assertTrue(foundF7);
    }

}
