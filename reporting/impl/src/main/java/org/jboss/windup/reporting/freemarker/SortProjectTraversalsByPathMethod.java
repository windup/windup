package org.jboss.windup.reporting.freemarker;

import freemarker.template.TemplateModelException;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.comparator.ProjectTraversalRootFileComparator;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.util.ExecutionStatistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Takes a list of ProjectModels and orders them according to their path.
 * <p>
 * For example, ProjectModels with this structure:
 *
 * <ul>
 * <li>/CProject</li>
 * <li>/BProject</li>
 * <li>/AProject</li>
 * </ul>
 * <p>
 * Will be returned as:
 *
 * <ul>
 * <li>/AProject</li>
 * <li>/BProject</li>
 * <li>/CProject</li>
 * </ul>
 */
public class SortProjectTraversalsByPathMethod implements WindupFreeMarkerMethod {
    private static final String NAME = "sortProjectTraversalsByPathAscending";

    @Override
    public String getMethodName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Takes an Iterable<" + ProjectModelTraversal.class.getSimpleName() + "> and returns them, ordered alphabetically.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() != 1) {
            throw new TemplateModelException("Error, method expects one argument (Iterable<ProjectModelTraversal>)");
        }

        @SuppressWarnings("unchecked")
        Iterable<ProjectModelTraversal> projectTraversalIterable = FreeMarkerUtil.freemarkerWrapperToIterable(arguments.get(0));
        List<ProjectModelTraversal> projectTraversalList = new ArrayList<>();
        for (ProjectModelTraversal traversal : projectTraversalIterable) {
            projectTraversalList.add(traversal);
        }
        Collections.sort(projectTraversalList, new ProjectTraversalRootFileComparator());
        ExecutionStatistics.get().end(NAME);
        return projectTraversalList;
    }

    @Override
    public void setContext(GraphRewrite event) {
    }
}
