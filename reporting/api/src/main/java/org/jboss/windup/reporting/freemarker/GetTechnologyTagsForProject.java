package org.jboss.windup.reporting.freemarker;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.model.TechnologyTagModel;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.util.ExecutionStatistics;

import java.util.List;

/**
 * <p>
 * Gets all technology tags for the provided {@link ProjectModelTraversal} and all of its subprojects (eg, "EJB", "Web XML").
 * </p>
 * <p>
 * Example call:
 * </p>
 *
 * <pre>
 *  getTechnologyTagsForProjectTraversal(ProjectModelTraversal).
 * </pre>
 *
 * <p>
 * The method will return an Iterable containing {@link TechnologyTagModel} instances.
 * </p>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class GetTechnologyTagsForProject implements WindupFreeMarkerMethod {
    private static final String NAME = "getTechnologyTagsForProjectTraversal";
    private GraphContext context;

    @Override
    public void setContext(GraphRewrite event) {
        this.context = event.getGraphContext();
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() != 1) {
            throw new TemplateModelException("Error, method expects one argument (" + ProjectModelTraversal.class.getSimpleName() + ")");
        }
        StringModel stringModelArg = (StringModel) arguments.get(0);
        ProjectModelTraversal projectTraversal = (ProjectModelTraversal) stringModelArg.getWrappedObject();
        Iterable<TechnologyTagModel> result = new TechnologyTagService(this.context).findTechnologyTagsForProject(projectTraversal);
        ExecutionStatistics.get().end(NAME);
        return result;
    }

    @Override
    public String getMethodName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Takes a " + ProjectModel.class.getSimpleName()
                + " as a parameter and returns an Iterable<" + TechnologyTagModel.class.getSimpleName()
                + "> containing the technology tags for this project (and all subprojects).";
    }
}
