package org.jboss.windup.reporting.freemarker;

import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.reporting.model.TechnologyTagModel;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.util.ExecutionStatistics;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;

/**
 * Gets all technology tags for the provided {@link ProjectModel} and all of its subprojects (eg, "EJB", "Web XML").
 * 
 * Example call:
 * 
 * getTechnologyTagsForProject(ProjectModel).
 * 
 * The method will return an Iterable containing {@link TechnologyTagModel} instances.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class GetTechnologyTagsForProject implements WindupFreeMarkerMethod
{
    private static final String NAME = "getTechnologyTagsForProject";
    private GraphContext context;

    @Override
    public void setContext(GraphRewrite event)
    {
        this.context = event.getGraphContext();
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() != 1)
        {
            throw new TemplateModelException("Error, method expects one argument (" + ProjectModel.class.getSimpleName() + ")");
        }
        StringModel stringModelArg = (StringModel) arguments.get(0);
        ProjectModel projectModel = (ProjectModel) stringModelArg.getWrappedObject();
        Iterable<TechnologyTagModel> result = new TechnologyTagService(this.context).findTechnologyTagsForProject(projectModel);
        ExecutionStatistics.get().end(NAME);
        return result;
    }

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return "Takes a " + ProjectModel.class.getSimpleName()
                    + " as a parameter and returns an Iterable<" + TechnologyTagModel.class.getSimpleName()
                    + "> containing the technology tags for this project (and all subprojects).";
    }
}
