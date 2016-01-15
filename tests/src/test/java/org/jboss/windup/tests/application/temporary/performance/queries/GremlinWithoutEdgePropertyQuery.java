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
public class GremlinWithoutEdgePropertyQuery extends TestQuery
{

    public GremlinWithoutEdgePropertyQuery() {
        super();
    }

    public GremlinWithoutEdgePropertyQuery(GraphContext context)
    {
        super(context);
    }

    @Override public Iterable<FileModel> specificQuery()
    {
        List<FileModel> resultFileModels = new ArrayList<>();
        GremlinPipeline<Iterable<?>, Object> pipeline = new GremlinPipeline<>(getTheOnlyProjectModel().asVertex());
        pipeline.out(ProjectModel.PROJECT_MODEL_TO_FILE);
        pipeline.has(FileModel.FILE_NAME,  "hardcoded_name");
        for (Object vertex : pipeline)
        {
            Vertex v = (Vertex) vertex;
            resultFileModels.add(context.getFramed().frame(v,FileModel.class));
        }

        return resultFileModels;
    }
}
