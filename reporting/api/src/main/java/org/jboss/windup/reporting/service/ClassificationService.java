package org.jboss.windup.reporting.service;

import javax.inject.Inject;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ClassificationModel;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * Adds methods for loading and querying ClassificationModel related data.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class ClassificationService extends GraphService<ClassificationModel>
{
    public ClassificationService()
    {
        super(ClassificationModel.class);
    }

    @Inject
    public ClassificationService(GraphContext context)
    {
        super(context, ClassificationModel.class);
    }

    /**
     * Returns the total effort points in all of the {@link ClassificationModel}s associated with the files in this
     * project.
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
            classificationEffort += (Integer) v.getProperty(ClassificationModel.PROPERTY_EFFORT);
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

    public void attachClassification(FileModel fileModel, String classificationText, String description)
    {
        ClassificationModel model = getUnique(getTypedQuery()
                    .has(ClassificationModel.PROPERTY_CLASSIFICATION, classificationText));
        if (model == null)
        {
            model = create();
            model.setClassifiation(classificationText);
            model.setDescription(description);
        }
        model.addFileModel(fileModel);
    }
}
