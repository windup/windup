package org.jboss.windup.tests.application.temporary.performance.queries;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.files.org.jboss.windup.rules.general.IterableFilter;

import java.util.List;

/**
 * Created by mbriskar on 1/15/16.
 */
public abstract class TestQuery
{
    protected GraphContext context;
    private long totalQueryTime =0;

    public TestQuery() {

    }

    public void setContext(GraphContext context) {
        this.context=context;
    }
    public TestQuery(GraphContext context) {
        this.context=context;
    }
    public abstract Iterable<FileModel> specificQuery();

    public Iterable<FileModel> query() {
        long startTime = System.nanoTime();
        Iterable<FileModel> resultFileModels = specificQuery();
        long queryTime = System.nanoTime() - startTime;
        this.totalQueryTime +=queryTime;
        return resultFileModels;
    }

    protected ProjectModel getTheOnlyProjectModel() {
        GraphService<ProjectModel> service = new GraphService<ProjectModel>(context, ProjectModel.class);
        return service.findAll().iterator().next();
    }

    protected GraphContext getGraphContext() {
        return context;
    }

    public String getTotalTimeReport() {
        return "Getting output through edges with Gremlin took " + (totalQueryTime) / 1000 + " ms.";
    }
}
