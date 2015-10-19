package org.jboss.windup.rules.apps.java.reporting.rules;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.ProjectModel;

/**
 * A ProjectModel with links to the unparsable files, for the UnparsableAppReportModel.
 *
 * @author Ondrej Zizka
 */
@TypeValue(ProjectWithUnparsablesModel.TYPE)
public interface ProjectWithUnparsablesModel extends ProjectModel
{
    public static final String TYPE = "ProjectWithUnparsables";
    public static final String UNPARSABLE_FILE = TYPE + ":unparsable";

    /**
     * Files that had problems while parsing.
     */
    //@Adjacency(label = UNPARSABLE_FILE)
    @GremlinGroovy("it.out('" + ProjectModel.PROJECT_MODEL_TO_FILE + "').has('" + FileModel.PARSE_ERROR + "')")
    Iterable<FileModel> getUnparsableFiles();
}
