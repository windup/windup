package org.jboss.windup.rules.apps.java.reporting.freemarker;

import java.util.List;
import java.util.logging.Logger;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.reporting.model.ApplicationReportIndexModel;
import org.jboss.windup.reporting.service.ApplicationReportIndexService;
import org.jboss.windup.rules.apps.java.archives.model.IdentifiedArchiveModel;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.Logging;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;

/**
 * Returns the SHA1 hash of the given a {@link ProjectModel}.
 * <p>
 * The function takes one parameter, and can be called from a freemarker template as follows:
 * <p>
 * projectModelToApplicationArchive(projectModel)
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
public class ProjectModelToSha1 implements WindupFreeMarkerMethod {
    private static Logger LOG = Logging.get(ProjectModelToSha1.class);

    private static final String NAME = "projectModelToSha1";

    private ApplicationReportIndexService service;

    @Override
    public void setContext(GraphRewrite event) {
        this.service = new ApplicationReportIndexService(event.getGraphContext());
    }

    @Override
    public String getMethodName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Takes a parameter of type " + ProjectModel.class.getSimpleName() + " and returns the associated "
                + ApplicationReportIndexModel.class.getSimpleName() + ".";
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
        if (projectModel.getRootFileModel() instanceof IdentifiedArchiveModel) {
            result = projectModel.getRootFileModel().getSHA1Hash();
        }
        ExecutionStatistics.get().end(NAME);

        return result;
    }
}
