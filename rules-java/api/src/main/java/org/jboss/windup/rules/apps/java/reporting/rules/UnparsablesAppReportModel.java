package org.jboss.windup.rules.apps.java.reporting.rules;

import org.apache.tinkerpop.gremlin.structure.Direction;
import com.syncleus.ferma.annotations.Adjacency;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.reporting.model.ApplicationReportModel;

import org.jboss.windup.graph.model.TypeValue;

/**
 * Model of the Unparsable Files report.
 *
 * @author Ondrej Zizka
 */
@TypeValue(UnparsablesAppReportModel.TYPE)
public interface UnparsablesAppReportModel extends ApplicationReportModel
{
    String TYPE = "UnparsablesAppReportModel";
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
