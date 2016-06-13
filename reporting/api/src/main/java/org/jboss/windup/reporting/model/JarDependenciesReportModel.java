package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.model.ProjectModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Incidence;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * This represents Jar Dependencies report model
 * 
 * @author mnovotny
 *
 */
@TypeValue(JarDependenciesReportModel.TYPE)
public interface JarDependenciesReportModel extends ApplicationReportModel
{
    String TYPE = "jarDependenciesReport";

    String JAR_REPORT_TO_PROJECT_MODEL = "jarDepsToProjectModel"; 

    /**
     * Contains all {@link ProjectModel}s that contain this file
     */
    @Incidence(label = JAR_REPORT_TO_PROJECT_MODEL, direction = Direction.OUT)
    Iterable<JarDependencyReportToProjectEdgeModel> getProjectEdges();

    /**
     * Adds {@link ProjectModel} to an edge for a dependency shown in Jar Dependency report
     */
    @Incidence(label = JAR_REPORT_TO_PROJECT_MODEL, direction = Direction.OUT)
    JarDependencyReportToProjectEdgeModel addProjectModel(ProjectModel project);
}