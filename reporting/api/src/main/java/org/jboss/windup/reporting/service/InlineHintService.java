package org.jboss.windup.reporting.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.apache.tools.ant.taskdefs.Length.FileMode;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.TagUtil;
import org.jboss.windup.reporting.model.EffortReportModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.Severity;
import org.jboss.windup.rules.files.model.FileReferenceModel;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Compare;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.structures.FramedVertexIterable;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;

import javax.annotation.Nullable;

/**
 * This provides helper functions for finding and creating {@link InlineHintModel} instances within the graph.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class InlineHintService extends GraphService<InlineHintModel>
{
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
     * Returns the total effort points in all of the {@link InlineHintModel} instances associated with the {@link FileMode} instances in the given
     * {@link ProjectModel}.
     * </p>
     * <p>
     * If set to recursive, then also include the effort points from child projects.
     * </p>
     * <p>
     * The result is a Map, the key contains the effort level and the value contains the number of incidents.
     * </p>
     */
    public Map<Integer, Integer> getMigrationEffortByPoints(ProjectModelTraversal traversal, Set<String> includeTags, Set<String> excludeTags,
                                                            boolean recursive, boolean includeZero)
    {
        final Map<Integer, Integer> results = new HashMap<>();

        EffortAccumulatorFunction accumulator = new EffortAccumulatorFunction()
        {
            @Override
            public void accumulate(Vertex effortReportVertex)
            {
                Integer migrationEffort = effortReportVertex.getProperty(EffortReportModel.EFFORT);
                if (!results.containsKey(migrationEffort))
                    results.put(migrationEffort, 1);
                else
                    results.put(migrationEffort, results.get(migrationEffort) + 1);
            }
        };

        getMigrationEffortDetails(traversal, includeTags, excludeTags, recursive, includeZero, accumulator);

        return results;
    }

    /**
     * <p>
     * Returns the total incidents in all of the {@link InlineHintModel}s associated with the files in this project by severity.
     * </p>
     */
    public Map<Severity, Integer> getMigrationEffortBySeverity(ProjectModelTraversal traversal, Set<String> includeTags, Set<String> excludeTags,
                boolean recursive)
    {
        final Map<Severity, Integer> results = new HashMap<>();

        EffortAccumulatorFunction accumulator = new EffortAccumulatorFunction()
        {
            @Override
            public void accumulate(Vertex effortReportVertex)
            {
                Severity severity = frame(effortReportVertex).getSeverity();
                if (!results.containsKey(severity))
                    results.put(severity, 1);
                else
                    results.put(severity, results.get(severity) + 1);
            }
        };

        getMigrationEffortDetails(traversal, includeTags, excludeTags, recursive, true, accumulator);

        return results;
    }

    private void getMigrationEffortDetails(ProjectModelTraversal traversal, Set<String> includeTags, Set<String> excludeTags, boolean recursive,
                boolean includeZero, EffortAccumulatorFunction accumulatorFunction)
    {

        final Set<Vertex> initialVertices = traversal.getAllProjectsAsVertices(recursive);

        GremlinPipeline<Vertex, Vertex> inlineHintPipeline = new GremlinPipeline<>(getGraphContext().getGraph());
        inlineHintPipeline.V();
        if (!includeZero)
        {
            inlineHintPipeline.has(EffortReportModel.EFFORT, Compare.GREATER_THAN, 0);
            inlineHintPipeline.has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, InlineHintModel.TYPE);
        }
        else
        {
            inlineHintPipeline.has(WindupVertexFrame.TYPE_PROP, InlineHintModel.TYPE);
        }

        inlineHintPipeline.as("hint");
        inlineHintPipeline.out(InlineHintModel.FILE_MODEL);
        inlineHintPipeline.in(ProjectModel.PROJECT_MODEL_TO_FILE);
        inlineHintPipeline.filter(new PipeFunction<Vertex, Boolean>()
        {
            @Override
            public Boolean compute(Vertex argument)
            {
                return initialVertices.contains(argument);
            }
        });
        inlineHintPipeline.back("hint");

        for (Vertex v : inlineHintPipeline)
        {
            // only check tags if we have some passed in
            if (!includeTags.isEmpty() || !excludeTags.isEmpty())
            {
                InlineHintModel hintModel = frame(v);
                if (!TagUtil.checkMatchingTags(hintModel.getTags(), includeTags, excludeTags))
                    continue;

            }
            accumulatorFunction.accumulate(v);
        }
    }
}
