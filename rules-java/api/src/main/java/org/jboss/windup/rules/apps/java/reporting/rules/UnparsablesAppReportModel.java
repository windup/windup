package org.jboss.windup.rules.apps.java.reporting.rules;

import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import com.tinkerpop.pipes.util.structures.Table;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.archives.model.IgnoredArchiveModel;

/**
 * Model of the Unparsable Files report.
 *
 * @author Ondrej Zizka
 */
@TypeValue(UnparsablesAppReportModel.TYPE)
public interface UnparsablesAppReportModel extends ApplicationReportModel
{
    String TYPE = "UnparsablesAppReport";
    String LABEL_PROJECT = TYPE + ":project";

    @Adjacency(label = LABEL_PROJECT)
    void addProject(ProjectModel project);

    @Adjacency(label = LABEL_PROJECT)
    Iterable<ProjectModel> getProjects();

    /**
     * Files that had problems while parsing.
     */
    @GremlinGroovy("it"
            + ".out('"+REPORT_TO_PROJECT_MODEL+"')"
            + ".as('x')"
            + ".in('"+ProjectModel.PARENT_PROJECT+"')"
            + ".simplePath"
            + ".loop('x'){true}{true}"
            + ".out('"+ProjectModel.ROOT_FILE_MODEL+"')"
            + ".hasNot('"+WindupVertexFrame.TYPE_PROP+"', '"+IgnoredArchiveModel.TYPE+"')"
            + ".back(2)")
    Iterable<ProjectModel> getAllSubProjects();

    @GremlinGroovy(frame = false, value = "it.out('"+REPORT_TO_PROJECT_MODEL+"').as('x')"
            + ".in('"+ProjectModel.PARENT_PROJECT+"')"
            + ".simplePath"
            + ".loop('x'){true}{true}"
            + ".as('prj')"
            + ".out('" + ProjectModel.PROJECT_MODEL_TO_FILE + "').has('" + FileModel.PARSE_ERROR + "').as('file')"
            + ".table.cap"
    )
    Object getAllSubProjectsAndTheirUnparsablesTable();

}
