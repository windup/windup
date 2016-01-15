package org.jboss.windup.tests.application.temporary.performance.queries;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;

/**
 * Created by mbriskar on 1/15/16.
 */
public class StandardWithoutEdgePropertyQuery extends TestQuery
{
    public StandardWithoutEdgePropertyQuery() {
        super();
    }

    public StandardWithoutEdgePropertyQuery(GraphContext context)
    {
        super(context);
    }

    @Override public Iterable<FileModel> specificQuery()
    {
        List<FileModel> resultFileModels = new ArrayList<>();
        for (Vertex file : getTheOnlyProjectModel().asVertex().getVertices(Direction.OUT, ProjectModel.PROJECT_MODEL_TO_FILE))
        {
            if (file.getProperty(FileModel.FILE_NAME).toString().contains("prefix"))
            {
                resultFileModels.add(context.getFramed().frame(file, FileModel.class));
            }
        }
        return resultFileModels;
    }
}
