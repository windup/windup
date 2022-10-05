package org.jboss.windup.reporting.freemarker;

import java.util.List;

import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.traversal.ArchiveSHA1ToFilePathMapper;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.util.ExecutionStatistics;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;

/**
 * <p>
 * Gets an instance of {@link ArchiveSHA1ToFilePathMapper} that is used for mapping from
 * a SHA1 String to a list of paths where this archive can be found.
 * </p>
 *
 * <p>
 * Example call:
 *
 * <pre>
 *       getArchiveSHA1ToPathMapper(traversal:ProjectModelTraversal)
 *   </pre>
 * </p>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class GetArchiveSHA1ToPathsMapperMethod implements WindupFreeMarkerMethod {

    public static final String NAME = "getArchiveSHA1ToPathMapper";

    @Override
    public String getMethodName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Gets an instance of a mapper for converting from the SHA1 hash of an archive to a List of file paths.";
    }

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() < 1) {
            throw new TemplateModelException("Error, method expects at least one argument (" + ProjectModel.class.getSimpleName() + ")");
        }
        StringModel stringModelArg = (StringModel) arguments.get(0);
        ProjectModelTraversal traversal = (ProjectModelTraversal) stringModelArg.getWrappedObject();
        ArchiveSHA1ToFilePathMapper mapper = new ArchiveSHA1ToFilePathMapper(traversal);

        ExecutionStatistics.get().end(NAME);
        return mapper;
    }
}
