package org.jboss.windup.reporting.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.tools.ant.taskdefs.Length.FileMode;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.TagUtil;
import org.jboss.windup.reporting.model.EffortReportModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.rules.files.model.FileReferenceModel;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Compare;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.structures.FramedVertexIterable;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;

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

        GremlinPipeline<Vertex, Vertex> inlineHintPipeline = new GremlinPipeline<>(initialVertices);
        inlineHintPipeline.out(ProjectModel.PROJECT_MODEL_TO_FILE);
        inlineHintPipeline.in(InlineHintModel.FILE_MODEL).has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, InlineHintModel.TYPE);

        List<InlineHintModel> results = new ArrayList<>();
        for (Vertex v : inlineHintPipeline)
        {
            results.add(frame(v));
        }
        return results;
    }

    /**
     * Returns the total effort points in all of the {@link InlineHintModel} instances associated with the {@link FileMode} instances in the given
     * {@link ProjectModel}.
     * <p/>
     * If set to recursive, then also include the effort points from child projects.
     */
    public int getMigrationEffortPoints(ProjectModel initialProject, Set<String> includeTags, Set<String> excludeTags, boolean recursive)
    {
        final Set<Vertex> initialVertices = new HashSet<>();
        if (recursive)
        {
            for (ProjectModel projectModel1 : initialProject.getAllProjectModels())
                initialVertices.add(projectModel1.asVertex());
        }
        else
        {
            initialVertices.add(initialProject.asVertex());
        }

        GremlinPipeline<Vertex, Vertex> inlineHintPipeline = new GremlinPipeline<>(getGraphContext().getGraph());
        inlineHintPipeline.V();
        inlineHintPipeline.has(EffortReportModel.EFFORT, Compare.GREATER_THAN, 0);
        inlineHintPipeline.has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, InlineHintModel.TYPE);

        inlineHintPipeline.as("hint");
        inlineHintPipeline.out(InlineHintModel.FILE_MODEL);
        inlineHintPipeline.out(FileModel.FILE_TO_PROJECT_MODEL);
        inlineHintPipeline.filter(new PipeFunction<Vertex, Boolean>()
        {
            @Override
            public Boolean compute(Vertex argument)
            {
                return initialVertices.contains(argument);
            }
        });
        inlineHintPipeline.back("hint");

        int hintEffort = 0;
        for (Vertex v : inlineHintPipeline)
        {
            // only check tags if we have some passed in
            if (!includeTags.isEmpty() || !excludeTags.isEmpty())
            {
                InlineHintModel hintModel = frame(v);
                if (!TagUtil.isTagsMatch(hintModel.getTags(), includeTags, excludeTags))
                    continue;
            }

            hintEffort += (Integer) v.getProperty(EffortReportModel.EFFORT);
        }
        return hintEffort;
    }
}
