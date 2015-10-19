package org.jboss.windup.rules.apps.java.reporting.rules;

import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.resource.FileModel;

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
            + ".in('"+ProjectModel.PARENT_PROJECT+"')"
            + ".loop(1)"
            + ".out('" + ProjectModel.PROJECT_MODEL_TO_FILE + "').has('" + FileModel.PARSE_ERROR + "')")
    Iterable<FileModel> getUnparsableFiles();

    @GremlinGroovy("it"
            + ".out('"+REPORT_TO_PROJECT_MODEL+"')"
            + ".as('x').in('"+ProjectModel.PARENT_PROJECT+"')"
            + ".simplePath"
            + ".loop('x'){true}"
            + ".out('" + ProjectModel.PROJECT_MODEL_TO_FILE + "').has('" + FileModel.PARSE_ERROR + "')")
    Iterable<ProjectModel> getUnparsableFiles2();


    @GremlinGroovy("it"
            + ".out('"+REPORT_TO_PROJECT_MODEL+"')")
    Iterable<ProjectModel> getReportToProject();

    @GremlinGroovy("it"
            + ".out('"+REPORT_TO_PROJECT_MODEL+"')"
            + ".in('"+ProjectModel.PARENT_PROJECT+"')")
    Iterable<ProjectModel> getChildProjects();

    /**
     * Files that had problems while parsing.
     */
    @GremlinGroovy("it"
            + ".out('"+REPORT_TO_PROJECT_MODEL+"')"
            + ".in('"+ProjectModel.PARENT_PROJECT+"')"
            + ".loop(1){true}{true}")
    Iterable<ProjectModel> getAllProjects();

    @GremlinGroovy("it"
            + ".out('"+REPORT_TO_PROJECT_MODEL+"')"
            + ".in('"+ProjectModel.PARENT_PROJECT+"')"
            + ".simplePath"
            + ".loop(2){true}")
    Iterable<ProjectModel> getAllProjects2();

    @GremlinGroovy("it"
            + ".out('"+REPORT_TO_PROJECT_MODEL+"')"
            + ".as('x').in('"+ProjectModel.PARENT_PROJECT+"')"
            + ".simplePath"
            + ".loop('x'){true}")
    Iterable<ProjectModel> getAllProjects3();

}
