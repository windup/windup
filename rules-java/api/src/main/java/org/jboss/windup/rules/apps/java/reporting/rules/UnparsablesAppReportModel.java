package org.jboss.windup.rules.apps.java.reporting.rules;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.reporting.model.ApplicationReportModel;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Model of the Unparsable Files report.
 *
 * @author Ondrej Zizka
 */
@TypeValue(UnparsablesAppReportModel.TYPE)
public interface UnparsablesAppReportModel extends ApplicationReportModel
{
    String TYPE = "UnparsablesAppReport";
    String ALL_SUB_PROJECTS = "allSubProjects";

    /**
     * All related (canonical) projects.
     */
    @Adjacency(label = ALL_SUB_PROJECTS, direction = Direction.OUT)
    Iterable<ProjectModel> getAllSubProjects();

    /**
     * All related (canonical) projects.
     */
    @Adjacency(label = ALL_SUB_PROJECTS, direction = Direction.OUT)
    void setAllSubProjects(Iterable<ProjectModel> projects);
}
