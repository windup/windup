package org.jboss.windup.tests.application.temporary.performance.queries;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mbriskar on 1/15/16.
 */
public class GremlinWithEdgePropertyQuery extends TestQuery
{

    public GremlinWithEdgePropertyQuery(GraphContext context)
    {
        super(context);
    }

    @Override public Iterable<FileModel> specificQuery()
    {
        List<FileModel> resultFileModels = new ArrayList<>();
        GremlinPipeline<Iterable<?>, Object> pipeline = new GremlinPipeline<>(getTheOnlyProjectModel().asVertex());
        pipeline.outE(ProjectModel.TO_FILE_MODEL_EDGE);
        pipeline.has(FileModel.FILE_NAME, Text.CONTAINS, "prefix");
        pipeline.outV();
        for (Object vertex : pipeline)
        {
            Vertex v = (Vertex) vertex;
            resultFileModels.add(context.getFramed().frame(v,FileModel.class));
        }

        return resultFileModels;
    }

}
