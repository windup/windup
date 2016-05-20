package org.jboss.windup.reporting.freemarker;

import java.util.List;

import freemarker.ext.beans.StringModel;
import org.jboss.windup.config.GraphRewrite;

import freemarker.template.TemplateModelException;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.ProjectModelTraversal;
import org.jboss.windup.util.ExecutionStatistics;

/**
 * Gets a {@link ProjectModelTraversal} instance for the given {@link ProjectModel}. In order for this to be effective,
 * the project passed in should be a "root" project model.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class GetProjectTraversalMethod implements WindupFreeMarkerMethod
{
    public static final String NAME = "getProjectTraversal";

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return "Gets a ProjectModelTraversal for the given ProjectModel.";
    }

    @Override
    public Object exec(List arguments) throws TemplateModelException
    {
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() != 1)
        {
            throw new TemplateModelException("Error, method expects one argument (" + ProjectModel.class.getSimpleName() + ")");
        }
        StringModel stringModelArg = (StringModel) arguments.get(0);
        ProjectModel projectModel = (ProjectModel) stringModelArg.getWrappedObject();
        ProjectModelTraversal traversal = new ProjectModelTraversal(projectModel);
        ExecutionStatistics.get().end(NAME);
        return traversal;
    }

    @Override
    public void setContext(GraphRewrite event)
    {

    }
}
