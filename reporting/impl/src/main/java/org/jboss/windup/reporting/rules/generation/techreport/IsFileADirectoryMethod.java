package org.jboss.windup.reporting.rules.generation.techreport;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.exception.WindupException;

import java.util.List;
import java.util.logging.Logger;

public class IsFileADirectoryMethod implements WindupFreeMarkerMethod {
    public static final Logger LOG = Logger.getLogger(IsFileADirectoryMethod.class.getName());
    private static final String NAME = "isFileADirectory";

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
        return "Takes a " + FileModel.class.getSimpleName()
                + " and returns a boolean of whether it is a Directory";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        ExecutionStatistics.get().begin(NAME);

        // Function arguments
        FileModel fileModel = null;

        // The file may *not* be null.
        if (arguments.size() >= 1) {
            StringModel fileArg = (StringModel) arguments.get(0);
            fileModel = (FileModel) fileArg.getWrappedObject();
        }

        if (fileModel == null) {
            String errorMessage = "IsFileADirectoryMethod: No file present to check";
            throw new WindupException(errorMessage);
        }

        Boolean result = fileModel.isDirectory();

        ExecutionStatistics.get().end(NAME);
        return result;
    }
}
