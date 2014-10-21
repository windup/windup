package org.jboss.windup.reporting.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ClassificationModel;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.structures.FramedVertexIterable;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * Adds methods for loading and querying ClassificationModel related data.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class ClassificationService extends GraphService<ClassificationModel>
{
    public ClassificationService(GraphContext context)
    {
        super(context, ClassificationModel.class);
    }

    /**
     * Returns the total effort points in all of the {@link ClassificationModel}s associated with the provided {@link FileModel}.
     * 
     */
    public int getMigrationEffortPoints(FileModel fileModel)
    {
        // 1. Get all classification models
        GremlinPipeline<Vertex, Vertex> classificationPipeline = new GremlinPipeline<>(fileModel.asVertex());
        classificationPipeline.in(ClassificationModel.FILE_MODEL);
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
     * Returns the total effort points in all of the {@link ClassificationModel}s associated with the files in this project.
     * 
     * If set to recursive, then also include the effort points from child projects.
     * 
     */
    public int getMigrationEffortPoints(ProjectModel projectModel, boolean recursive)
    {
        // 1. Get all classification models
        GremlinPipeline<Vertex, Vertex> classificationPipeline = new GremlinPipeline<>(projectModel.asVertex());
        classificationPipeline.out(ProjectModel.PROJECT_MODEL_TO_FILE).in(ClassificationModel.FILE_MODEL);
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
        if (recursive)
        {
            for (ProjectModel childProject : projectModel.getChildProjects())
            {
                classificationEffort += getMigrationEffortPoints(childProject, recursive);
            }
        }
        return classificationEffort;
    }

    /**
     * Returns the {@link ClassificationModel} with the specified classification text.
     */
    public ClassificationModel getByClassification(String classificationText)
    {
        return getUnique(getTypedQuery().has(ClassificationModel.CLASSIFICATION, classificationText));
    }

    /**
     * Attach a {@link ClassificationModel} with the given classificationText and description to the provided {@link FileModel}. If an existing Model
     * exists with the provided classificationText, that one will be used instead.
     */
    public ClassificationModel attachClassification(FileModel fileModel, String classificationText, String description)
    {
        ClassificationModel model = getByClassification(classificationText);
        if (model == null)
        {
            model = create();
            model.setClassifiation(classificationText);
            model.setDescription(description);
        }
        model.addFileModel(fileModel);
        return model;
    }

    /**
     * Returns all {@link ClassificationModel}s for the given {@link FileModel}.
     */
    public Iterable<ClassificationModel> getClassificationModelsForFile(FileModel fileModel)
    {
        GremlinPipeline<Vertex, Vertex> classificationPipeline = new GremlinPipeline<>(fileModel.asVertex());
        classificationPipeline.in(ClassificationModel.FILE_MODEL);
        classificationPipeline.has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, ClassificationModel.TYPE);

        return new FramedVertexIterable<>(getGraphContext().getFramed(), classificationPipeline, getType());
    }

    /**
     * Returns all {@link ClassificationModel}s for the given {@link FileModel} with the specified Classification text.
     */
    public Iterable<ClassificationModel> getClassificationModelsForFile(FileModel fileModel, String classification)
    {
        GremlinPipeline<Vertex, Vertex> classificationPipeline = new GremlinPipeline<>(fileModel.asVertex());
        classificationPipeline.in(ClassificationModel.FILE_MODEL);
        classificationPipeline.has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, ClassificationModel.TYPE);
        classificationPipeline.has(ClassificationModel.CLASSIFICATION, classification);

        return new FramedVertexIterable<>(getGraphContext().getFramed(), classificationPipeline, getType());
    }
}
