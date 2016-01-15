package org.jboss.windup.tests.application.temporary.performance.queries;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.ToFileModelEdge;

import java.util.ArrayList;
import java.util.List;

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
        for (ToFileModelEdge fileModelEdge : getTheOnlyProjectModel().getToFileModelEdges())
        {
            if (fileModelEdge.getFileName().contains("prefix"))
            {
                resultFileModels.add(fileModelEdge.getFileModel());
            }
        }
        return resultFileModels;
    }
}
