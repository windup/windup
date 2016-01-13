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
public class StandardWithoutEdgePropertyQuery extends TestQuery
{
    public StandardWithoutEdgePropertyQuery(GraphContext context)
    {
        super(context);
    }

    @Override public Iterable<FileModel> specificQuery()
    {
        List<FileModel> resultFileModels = new ArrayList<>();
        for (FileModel fileModel : getTheOnlyProjectModel().getFileModels())
        {
            if (fileModel.getFileName().contains("prefix"))
            {
                resultFileModels.add(fileModel);
            }
        }
        return resultFileModels;
    }
}
