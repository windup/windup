package org.jboss.windup.rules.apps.java.reporting.freemarker.filepath;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.ReportResourceFileModel;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.util.ExecutionStatistics;

import java.util.List;

/**
 * A template pattern: An abstract class implementing a template method exec for multiple path generators.
 * In order to provide a new path generator from Model, extend this class and implement all the abstract methods.
 */
public abstract class AbstractGetPrettyPathForFile implements WindupFreeMarkerMethod {
    private static final String NAME = "abstractGetPrettyPathForFile";

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        ExecutionStatistics.get().begin(NAME);
        try {
            if (arguments.size() != 1) {
                throw new TemplateModelException("Error, method expects one argument (FileModel)");
            }
            StringModel stringModelArg = (StringModel) arguments.get(0);
            FileModel fileModel = (FileModel) stringModelArg.getWrappedObject();
            if (fileModel instanceof JavaClassFileModel) {
                return getPath((JavaClassFileModel) fileModel);
            } else if (fileModel instanceof ReportResourceFileModel) {
                return getPath((ReportResourceFileModel) fileModel);
            } else if (fileModel instanceof JavaSourceFileModel) {
                return getPath((JavaSourceFileModel) fileModel);
            } else {
                return getPath(fileModel);
            }
        } finally {
            ExecutionStatistics.get().end(NAME);
        }
    }

    public abstract String getPath(JavaClassFileModel jcfm);

    public abstract String getPath(ReportResourceFileModel model);

    public abstract String getPath(FileModel model);

    public abstract String getPath(JavaSourceFileModel javaSourceModel);

    @Override
    public String getDescription() {
        return "Takes a " + FileModel.class.getSimpleName()
                + " as a parameter and either the qualified name (if it is a Java file) or the path within the file's project.";
    }
}
