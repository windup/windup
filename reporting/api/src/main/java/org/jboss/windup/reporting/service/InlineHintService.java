package org.jboss.windup.reporting.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.FileReferenceModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.model.EffortReportModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.category.IssueCategoryModel;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Compare;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.structures.FramedVertexIterable;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import org.jboss.windup.reporting.model.IssueDisplayMode;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This provides helper functions for finding and creating {@link InlineHintModel} instances within the graph.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class InlineHintService extends GraphService<InlineHintModel>
{
    public static final Logger LOG = Logger.getLogger(InlineHintService.class.getName());

    public InlineHintService(GraphContext context)
    {
        super(context, InlineHintModel.class);
    }

    /**
     * Gets all {@link InlineHintModel} instances that are directly associated with the given {@link FileReferenceModel}
     */
    public Iterable<InlineHintModel> getHintsForFileReference(FileReferenceModel reference)
    {
        GremlinPipeline<Vertex, Vertex> inlineHintPipeline = new GremlinPipeline<>(reference.asVertex());
        inlineHintPipeline.in(InlineHintModel.FILE_LOCATION_REFERENCE);
        inlineHintPipeline.has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, InlineHintModel.TYPE);
        return new FramedVertexIterable<>(getGraphContext().getFramed(), inlineHintPipeline, InlineHintModel.class);
    }

    /**
     * Gets all {@link InlineHintModel} instances that are directly associated with the given {@link FileModel}
     */
    public Iterable<InlineHintModel> getHintsForFile(FileModel file)
    {
        GremlinPipeline<Vertex, Vertex> inlineHintPipeline = new GremlinPipeline<>(file.asVertex());
        inlineHintPipeline.in(FileReferenceModel.FILE_MODEL);
        inlineHintPipeline.has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, InlineHintModel.TYPE);
        return new FramedVertexIterable<>(getGraphContext().getFramed(), inlineHintPipeline, InlineHintModel.class);
    }

    /**
     * Returns the total effort points in all of the {@link InlineHintModel} instances associated with the provided {@link FileModel}.
     */
    public int getMigrationEffortPoints(FileModel fileModel)
    {
        GremlinPipeline<Vertex, Vertex> inlineHintPipeline = new GremlinPipeline<>(fileModel.asVertex());
        inlineHintPipeline.in(InlineHintModel.FILE_MODEL);
        inlineHintPipeline.has(EffortReportModel.EFFORT, Compare.GREATER_THAN, 0);
        inlineHintPipeline.has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, InlineHintModel.TYPE);

        int hintEffort = 0;
        for (Vertex v : inlineHintPipeline)
        {
            hintEffort += (Integer) v.getProperty(InlineHintModel.EFFORT);
        }
        return hintEffort;
    }

    private Collection<Vertex> getProjectAndChildren(ProjectModel projectModel)
    {
        ArrayList<Vertex> result = new ArrayList<>();
        result.add(projectModel.asVertex());

        for (ProjectModel child : projectModel.getChildProjects())
        {
            result.addAll(getProjectAndChildren(child));
        }
        return result;
    }

    /**
     * Returns all hints for the given {@link ProjectModel}. If recursive is set, then this will recurse into
     * child projects as well.
     */
    public Iterable<InlineHintModel> getHintsForProject(ProjectModel projectModel, boolean recursive)
    {
        final Iterable<Vertex> initialVertices;
        if (recursive)
        {
            initialVertices = getProjectAndChildren(projectModel);
        }
        else
        {
            initialVertices = Collections.singletonList(projectModel.asVertex());
        }

        return getInlineHintModels(initialVertices);
    }

    public Iterable<InlineHintModel> getHintsForProjects(Iterable<ProjectModel> projectModels)
    {
        Iterable<Vertex> projectVertexIterable = Iterables.transform(projectModels, new Function<ProjectModel, Vertex>()
        {
            @Override
            public Vertex apply(ProjectModel input)
            {
                return input.asVertex();
            }
        });
        return getInlineHintModels(projectVertexIterable);
    }

    private Iterable<InlineHintModel> getInlineHintModels(Iterable<Vertex> initialProjectVertices) {
        GremlinPipeline<Vertex, Vertex> inlineHintPipeline = new GremlinPipeline<>(initialProjectVertices);
        inlineHintPipeline.out(ProjectModel.PROJECT_MODEL_TO_FILE);
        inlineHintPipeline.in(InlineHintModel.FILE_MODEL).has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, InlineHintModel.TYPE);

        Set<InlineHintModel> results = new LinkedHashSet<>();
        for (Vertex v : inlineHintPipeline)
        {
            results.add(frame(v));
        }
        return results;
    }

    /**
     * <p>
     * Returns the total effort points in all of the {@link InlineHintModel}s
     * associated with the {@link FileModel} instances in the given {@link ProjectModelTraversal}.
     * </p>
     * <p>
     * If set to recursive, then also include the effort points from child projects.
     * </p>
     * <p>
     * The result is a Map, the key contains the effort level and the value contains the number of incidents.
     * </p>
     */
    public Map<Integer, Integer> getMigrationEffortByPoints(
        ProjectModelTraversal traversal, Set<String> includeTags, Set<String> excludeTags, boolean recursive, boolean includeZero)
    {
        MapSumEffortAccumulatorFunction<Integer> accumulator = new MapSumEffortAccumulatorFunction(){
            public Object vertexToKey(Vertex effortReportVertex) {
                Integer migrationEffort = effortReportVertex.getProperty(EffortReportModel.EFFORT);
                return migrationEffort;
            }
        };
        getMigrationEffortDetails(traversal, includeTags, excludeTags, recursive, includeZero, accumulator);
        return accumulator.getResults();
    }

    /**
     * Returns the total incidents in all of the {@link InlineHintModel}s associated with the files in this project by severity.
     */
    public Map<IssueCategoryModel, Integer> getMigrationEffortBySeverity(GraphRewrite event, ProjectModelTraversal traversal, Set<String> includeTags, Set<String> excludeTags,
                                                                    boolean recursive)
    {
        MapSumEffortAccumulatorFunction<IssueCategoryModel> accumulator = new MapSumEffortAccumulatorFunction<IssueCategoryModel>()
        {
            public IssueCategoryModel vertexToKey(Vertex effortReportVertex)
            {
                return frame(effortReportVertex).getIssueCategory();
            }

            @Override
            public void accumulate(Vertex effortReportVertex)
            {
                /*
                 * If it is a detail only issue, then summaries should not include it in the count.
                 */
                if (frame(effortReportVertex).getIssueDisplayMode() == IssueDisplayMode.DETAIL_ONLY)
                    return;

                super.accumulate(effortReportVertex);
            }
        };
        this.getMigrationEffortDetails(traversal, includeTags, excludeTags, recursive, true, accumulator);
        return accumulator.getResults();
    }

    private void getMigrationEffortDetails(ProjectModelTraversal traversal, Set<String> includeTags, Set<String> excludeTags, boolean recursive,
                boolean includeZero, EffortAccumulatorFunction accumulatorFunction)
    {
        LOG.log(Level.INFO, String.format("\n\t\t\tEFFORT H: getMigrationEffortDetails() with: %s, %srecur, %sincludeZero, %s, tags: %s, excl: %s",
                traversal, recursive ? "" : "!", includeZero ? "" : "!", accumulatorFunction, includeTags, excludeTags));

        final Set<Vertex> initialVertices = traversal.getAllProjectsAsVertices(recursive);

        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(this.getGraphContext().getGraph());
        pipeline.V();
        // If the multivalue index is not 1st, then it doesn't work - https://github.com/thinkaurelius/titan/issues/403
        if (!includeZero)
        {
            pipeline.has(EffortReportModel.EFFORT, Compare.GREATER_THAN, 0);
            pipeline.has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, InlineHintModel.TYPE);
        }
        else
        {
            pipeline.has(WindupVertexFrame.TYPE_PROP, InlineHintModel.TYPE);
        }
        pipeline.as("hint");
        pipeline.out(InlineHintModel.FILE_MODEL);
        pipeline.in(ProjectModel.PROJECT_MODEL_TO_FILE);
        pipeline.filter(new SetMembersFilter(initialVertices));
        pipeline.back("hint");

        boolean checkTags = !includeTags.isEmpty() || !excludeTags.isEmpty();
        for (Vertex v : pipeline)
        {
            // only check tags if we have some passed in
            if (checkTags && !frame(v).matchesTags(includeTags, excludeTags))
                continue;

            accumulatorFunction.accumulate(v);
        }
    }
}
