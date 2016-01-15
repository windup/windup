package org.jboss.windup.tests.application.temporary.performance.queries;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;

/**
 * Created by mbriskar on 1/15/16.
 */
public class StandardWithEdgePropertyQuery extends TestQuery
{

    public StandardWithEdgePropertyQuery() {
        super();
    }

    public StandardWithEdgePropertyQuery(GraphContext context)
    {
        super(context);
    }

    @Override public Iterable<FileModel> specificQuery()
    {
        List<FileModel> resultFileModels = new ArrayList<>();
        for (Edge fileModelEdge : getTheOnlyProjectModel().asVertex().getEdges(Direction.OUT, ProjectModel.TO_FILE_MODEL_EDGE))
        {
            if (fileModelEdge.getProperty(FileModel.FILE_NAME).toString().contains("prefix"))
            {
                FileModel fileModel = context.getFramed().frame(fileModelEdge.getVertex(Direction.OUT), FileModel.class);
                resultFileModels.add(fileModel);
            }
        }
        return resultFileModels;
    }
}
