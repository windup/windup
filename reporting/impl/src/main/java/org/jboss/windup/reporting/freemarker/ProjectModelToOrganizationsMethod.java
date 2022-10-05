package org.jboss.windup.reporting.freemarker;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.OrganizationModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.Logging;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;

/**
 * Given a {@link ProjectModel}, return the {@link Iterable<OrganizationModel>} that is associated with the application.
 * <p>
 * The function takes one parameter, and can be called from a freemarker template as follows:
 * <p>
 * projectModelToOrganizations(projectModel)
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
public class ProjectModelToOrganizationsMethod implements WindupFreeMarkerMethod {
    private static final Logger LOG = Logging.get(ProjectModelToOrganizationsMethod.class);

    private static final String NAME = "projectModelToOrganizations";

    @Override
    public void setContext(GraphRewrite event) {
    }

    @Override
    public String getMethodName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Takes a parameter of type " + ProjectModel.class.getSimpleName() + " and returns the associated "
                + OrganizationModel.class.getSimpleName() + "s.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        Object result = null;

        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() != 1) {
            throw new TemplateModelException("Error, method expects one argument (ProjectModel)");
        }
        StringModel stringModelArg = (StringModel) arguments.get(0);
        if (stringModelArg == null) {
            throw new IllegalArgumentException("FreeMarker Method " + NAME + " called with null project model");
        }
        ProjectModel projectModel = (ProjectModel) stringModelArg.getWrappedObject();

        if (projectModel.getRootFileModel() instanceof ArchiveModel) {
            result = ((ArchiveModel) projectModel.getRootFileModel()).getOrganizationModels();
        }

        ExecutionStatistics.get().end(NAME);

        if (result == null) {
            result = new ArrayList<>();
        }
        return result;
    }
}
