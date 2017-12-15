package org.jboss.windup.reporting.service;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.jboss.forge.furnace.util.Iterators;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.model.DefaultTechnologyTagComparator;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.model.TechnologyTagModel;

import com.thinkaurelius.titan.core.attribute.Text;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.frames.FramedGraphQuery;
import com.tinkerpop.frames.structures.FramedVertexIterable;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.util.structures.Pair;

/**
 * Contains methods for finding, creating, and deleting {@link TechnologyTagModel} instances.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * 
 */
public class TechnologyTagService extends GraphService<TechnologyTagModel>
{

    public TechnologyTagService(GraphContext context)
    {
        super(context, TechnologyTagModel.class);
    }

    /**
     * Adds the provided tag to the provided {@link FileModel}. If a {@link TechnologyTagModel} cannot be found with the provided name, then one will
     * be created.
     */
    public TechnologyTagModel addTagToFileModel(FileModel fileModel, String tagName, TechnologyTagLevel level)
    {
        FramedGraphQuery q = getGraphContext().getQuery().type(TechnologyTagModel.class)
                    .has(TechnologyTagModel.NAME, tagName);
        TechnologyTagModel technologyTag = super.getUnique(q);
        if (technologyTag == null)
        {
            technologyTag = create();
            technologyTag.setName(tagName);
            technologyTag.setLevel(level);
        }
        if (level == TechnologyTagLevel.IMPORTANT && fileModel instanceof SourceFileModel)
            ((SourceFileModel) fileModel).setGenerateSourceReport(true);

        technologyTag.addFileModel(fileModel);
        return technologyTag;
    }

    /**
     * Removes the provided tag from the provided {@link FileModel}. If a {@link TechnologyTagModel} cannot be found with the provided name, then this
     * operation will do nothing.
     */
    public void removeTagFromFileModel(FileModel fileModel, String tagName)
    {
        FramedGraphQuery q = getGraphContext().getQuery().type(TechnologyTagModel.class)
                    .has(TechnologyTagModel.NAME, tagName);
        TechnologyTagModel technologyTag = super.getUnique(q);

        if (technologyTag != null)
            technologyTag.removeFileModel(fileModel);
    }

    /**
     * Return an {@link Iterable} containing all {@link TechnologyTagModel}s that are directly associated with the provided {@link FileModel}.
     */
    public Iterable<TechnologyTagModel> findTechnologyTagsForFile(FileModel fileModel)
    {
        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(fileModel.asVertex());
        pipeline.in(TechnologyTagModel.TECH_TAG_TO_FILE_MODEL).has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, TechnologyTagModel.TYPE);
        pipeline.order(new PipeFunction<Pair<Vertex, Vertex>, Integer>()
        {
            private Comparator<TechnologyTagModel> comparator = new DefaultTechnologyTagComparator();

            @Override
            public Integer compute(Pair<Vertex, Vertex> argument)
            {
                return comparator.compare(frame(argument.getA()), frame(argument.getB()));
            }
        });
        return new FramedVertexIterable<>(getGraphContext().getFramed(), pipeline, TechnologyTagModel.class);
    }

    /**
     * Return an {@link Iterable} containing all {@link TechnologyTagModel}s that are directly associated with the provided {@link ProjectModel}.
     */
    public Iterable<TechnologyTagModel> findTechnologyTagsForProject(ProjectModelTraversal traversal)
    {
        Set<TechnologyTagModel> results = new TreeSet<>(new DefaultTechnologyTagComparator());

        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(traversal.getCanonicalProject().asVertex());
        pipeline.out(ProjectModel.PROJECT_MODEL_TO_FILE);
        pipeline.in(TechnologyTagModel.TECH_TAG_TO_FILE_MODEL).has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, TechnologyTagModel.TYPE);

        Iterable<TechnologyTagModel> modelIterable = new FramedVertexIterable<>(getGraphContext().getFramed(), pipeline,
                    TechnologyTagModel.class);
        results.addAll(Iterators.asSet(modelIterable));

        for (ProjectModelTraversal childTraversal : traversal.getChildren())
        {
            results.addAll(Iterators.asSet(findTechnologyTagsForProject(childTraversal)));
        }

        return results;
    }
}
