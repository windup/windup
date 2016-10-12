package org.jboss.windup.reporting.service;

import java.util.logging.Logger;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.EffortReportModel;
import org.jboss.windup.reporting.model.Severity;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Compare;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.structures.FramedVertexIterable;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import java.util.logging.Level;

/**
 * Adds methods for loading and querying ClassificationModel related data.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 *
 */
public class ClassificationService extends GraphService<ClassificationModel>
{
    public static final Logger LOG = Logger.getLogger(ClassificationService.class.getName());

    public ClassificationService(GraphContext context)
    {
        super(context, ClassificationModel.class);
    }

    /**
     * Returns the total effort points in all of the {@link ClassificationModel}s associated with the provided {@link FileModel}.
     */
    public int getMigrationEffortPoints(FileModel fileModel)
    {
        GremlinPipeline<Vertex, Vertex> classificationPipeline = new GremlinPipeline<>(fileModel.asVertex());
        classificationPipeline.in(ClassificationModel.FILE_MODEL);
        classificationPipeline.has(EffortReportModel.EFFORT, Compare.GREATER_THAN, 0);
        classificationPipeline.has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, ClassificationModel.TYPE);

        int classificationEffort = 0;
        for (Vertex v : classificationPipeline)
        {
            Integer migrationEffort = v.getProperty(ClassificationModel.EFFORT);
            if (migrationEffort != null)
            {
                classificationEffort += migrationEffort;
            }
        }
        return classificationEffort;
    }

    /**
     * Return all {@link ClassificationModel} instances that are attached to the given {@link FileModel} instance.
     */
    public Iterable<ClassificationModel> getClassifications(FileModel model)
    {
        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(model.asVertex());
        pipeline.in(ClassificationModel.FILE_MODEL);
        pipeline.has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, ClassificationModel.TYPE);
        return new FramedVertexIterable<>(getGraphContext().getFramed(), pipeline, ClassificationModel.class);
    }

    /**
     * Return all {@link ClassificationModel} instances that are attached to the given {@link FileModel} instance with a specific classification name.
     */
    public Iterable<ClassificationModel> getClassificationByName(FileModel model, String classificationName)
    {
        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(model.asVertex());
        pipeline.in(ClassificationModel.FILE_MODEL);
        pipeline.has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, ClassificationModel.TYPE);
        pipeline.has(ClassificationModel.CLASSIFICATION, classificationName);
        return new FramedVertexIterable<>(getGraphContext().getFramed(), pipeline, ClassificationModel.class);
    }

    /**
     * <p>
     * Returns the total effort points in all of the {@link ClassificationModel}s
     * associated with the {@link FileModel} instances in the given {@link ProjectModelTraversal}.
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
        MapSumEffortAccumulatorFunction<Integer> accumulator = new MapSumEffortAccumulatorFunction()
        {
            public Integer vertexToKey(Vertex effortReportVertex)
            {
                Integer migrationEffort = effortReportVertex.getProperty(EffortReportModel.EFFORT);
                return migrationEffort;
            }
        };
        getMigrationEffortDetails(traversal, includeTags, excludeTags, recursive, includeZero, accumulator);
        return accumulator.getResults();
    }

    /**
     * Returns the total incidents in all of the {@link ClassificationModel}s associated with the files in this project by severity.
     */
    public Map<Severity, Integer> getMigrationEffortBySeverity(
        ProjectModelTraversal traversal, Set<String> includeTags, Set<String> excludeTags, boolean recursive)
    {
        MapSumEffortAccumulatorFunction<Severity> accumulator = new MapSumEffortAccumulatorFunction()
        {
            public Severity vertexToKey(Vertex effortReportVertex)
            {
                return frame(effortReportVertex).getSeverity();
            }
        };
        this.getMigrationEffortDetails(traversal, includeTags, excludeTags, recursive, true, accumulator);
        return accumulator.getResults();
    }

    private void getMigrationEffortDetails(ProjectModelTraversal traversal, Set<String> includeTags, Set<String> excludeTags, boolean recursive,
                boolean includeZero, EffortAccumulatorFunction accumulatorFunction)
    {
        LOG.log(Level.INFO, String.format("\n\t\t\tEFFORT C: getMigrationEffortDetails() with: %s, %srecur, %sincludeZero, %s, tags: %s, excl: %s",
                traversal, recursive ? "" : "!", includeZero ? "" : "!", accumulatorFunction, includeTags, excludeTags));

        final Set<Vertex> initialVertices = traversal.getAllProjectsAsVertices(recursive);

        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(this.getGraphContext().getGraph());
        pipeline.V();
        // If the multivalue index is not 1st, then it doesn't work - https://github.com/thinkaurelius/titan/issues/403
        if (!includeZero)
        {
            pipeline.has(EffortReportModel.EFFORT, Compare.GREATER_THAN, 0);
            pipeline.has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, ClassificationModel.TYPE);
        }
        else
        {
            pipeline.has(WindupVertexFrame.TYPE_PROP, ClassificationModel.TYPE);
        }
        pipeline.as("classification");
        // For each classification, count it repeatedly for each file that is within given set of Projects (from the traversal).
        pipeline.out(ClassificationModel.FILE_MODEL);
        pipeline.in(ProjectModel.PROJECT_MODEL_TO_FILE);
        pipeline.filter(new SetMembersFilter(initialVertices));
        pipeline.back("classification");

        boolean checkTags = !includeTags.isEmpty() || !excludeTags.isEmpty();
        FileService fileService = new FileService(getGraphContext());
        for (Vertex v : pipeline)
        {
            // only check tags if we have some passed in
            if (checkTags && !frame(v).matchesTags(includeTags, excludeTags))
                continue;

            // For each classification, count it repeatedly for each file.
            // TODO: .accumulate(v, count);
            // TODO: This could be all done just within the query (provided that the tags would be taken care of).
            //       Accumulate could be a PipeFunction.
            for (Vertex fileVertex : v.getVertices(Direction.OUT, ClassificationModel.FILE_MODEL))
            {
                // Make sure that this file is actually in an accepted project. The pipeline condition will return
                // classifications that aren't necessarily in the same project.
                FileModel fileModel = fileService.frame(fileVertex);
                if (initialVertices.contains(fileModel.getProjectModel().asVertex()))
                    accumulatorFunction.accumulate(v);
            }
        }
    }

    /**
     * Attach a {@link ClassificationModel} with the given classificationText and description to the provided {@link FileModel}. If an existing Model
     * exists with the provided classificationText, that one will be used instead.
     */
    public ClassificationModel attachClassification(Rule rule, FileModel fileModel, String classificationText, String description)
    {
        ClassificationModel model = getUnique(getTypedQuery().has(ClassificationModel.CLASSIFICATION, classificationText));
        if (model == null)
        {
            model = create();
            model.setClassification(classificationText);
            model.setDescription(description);
            model.setEffort(0);
            model.setRuleID(rule.getId());
            model.addFileModel(fileModel);
            if (fileModel instanceof SourceFileModel)
                ((SourceFileModel) fileModel).setGenerateSourceReport(true);

            return model;
        }

        return attachClassification(model, fileModel);
    }

    /**
     * Attach a {@link ClassificationModel} with the given classificationText and description to the provided {@link FileModel}. If an existing Model
     * exists with the provided classificationText, that one will be used instead.
     */
    public ClassificationModel attachClassification(EvaluationContext context, FileModel fileModel, String classificationText, String description)
    {
        Rule rule = (Rule) context.get(Rule.class);
        return attachClassification(rule, fileModel, classificationText, description);
    }

    private boolean isClassificationLinkedToFileModel(ClassificationModel classificationModel, FileModel fileModel)
    {
        return ClassificationServiceCache.isClassificationLinkedToFileModel(classificationModel, fileModel);
    }

    /**
     * This method just attaches the {@link ClassificationModel} to the {@link FileModel}.
     * It will only do so if this link is not already present.
     */
    public ClassificationModel attachClassification(ClassificationModel classificationModel, FileModel fileModel)
    {
        if (!isClassificationLinkedToFileModel(classificationModel, fileModel))
        {
            classificationModel.addFileModel(fileModel);
            if (fileModel instanceof SourceFileModel)
                ((SourceFileModel) fileModel).setGenerateSourceReport(true);
        }
        ClassificationServiceCache.cacheClassificationFileModel(classificationModel, fileModel, true);

        return classificationModel;
    }

    /**
     * Attach the given link to the classification, while checking for duplicates.
     */
    public ClassificationModel attachLink(ClassificationModel classificationModel, LinkModel linkModel)
    {
        for (LinkModel existing : classificationModel.getLinks())
        {
            if (StringUtils.equals(existing.getLink(), linkModel.getLink()))
            {
                return classificationModel;
            }
        }
        classificationModel.addLink(linkModel);
        return classificationModel;
    }
}
