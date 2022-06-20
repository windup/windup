package org.jboss.windup.reporting.rules.generation.techreport;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.exception.WindupException;

import java.util.List;
import java.util.logging.Logger;

/**
 * Gets the count of library occurences recursively per input application.
 *
 * <p> Called from a freemarker template as follows:
 *
 * <pre>
 *      getNumberOfLibraries( projectToCount: ProjectModel ): Integer
 * </pre>
 *
 * <p> Returns an Integer object, which holds the count:
 *
 * @author <a href="mailto:mbrophy@redhat.com">Mark Brophy</a>
 */
public class GetNumberOfLibrariesMethod implements WindupFreeMarkerMethod {
    public static final Logger LOG = Logger.getLogger(GetNumberOfLibrariesMethod.class.getName());
    private static final String NAME = "getNumberOfLibraries";

    private GraphContext graphContext;

    @Override
    public void setContext(GraphRewrite event) {
        this.graphContext = event.getGraphContext();
    }

    @Override
    public String getMethodName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Takes a " + ProjectModel.class.getSimpleName()
                + " as a parameter and returns a count of the number of libraries contained within";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        ExecutionStatistics.get().begin(NAME);

        // Function arguments
        ProjectModel projectModel = null;

        // The project. May *not* be null. All input applications should be passed into this method individually
        if (arguments.size() >= 1) {
            StringModel projectArg = (StringModel) arguments.get(0);
            projectModel = (ProjectModel) projectArg.getWrappedObject();
        }

        if (projectModel == null) {
            String errorMessage = "GetNumberOfLibrariesMethod: No project present to count libraries";
            throw new WindupException(errorMessage);
        }

        Integer result = countLibrariesInModel(this.graphContext, projectModel);

        ExecutionStatistics.get().end(NAME);
        return result;
    }

    private Integer countLibrariesInModel(GraphContext graphContext, ProjectModel projectModel) {
        int count = 0;
        for (ProjectModel child : projectModel.getChildProjects()) {
            if (child.getRootFileModel() instanceof ArchiveModel) {
                count++;
                count += countLibrariesInModel(graphContext, child);
            }
        }
        return count;
    }
}
