package org.jboss.windup.rules.apps.java.dependencyreport;

import com.tinkerpop.frames.Adjacency;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ProjectModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Incidence;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.reporting.model.ApplicationReportModel;

/**
 * This represents Jar Dependencies report model
 * 
 * @author mnovotny
 *
 */
@TypeValue(DependenciesReportModel.TYPE)
public interface DependenciesReportModel extends ApplicationReportModel
{
    String TYPE = "jarDependenciesReport";

    String DEPENDENCY_REPORT_GROUP = "dependencyReportGroup";

    /**
     * Contains all dependencies reported here, grouped by hash.
     */
    @Adjacency(label = DEPENDENCY_REPORT_GROUP, direction = Direction.OUT)
    Iterable<DependencyReportDependencyGroupModel> getArchiveGroups();

    /**
     * Contains all dependencies reported here, grouped by hash.
     */
    @Adjacency(label = DEPENDENCY_REPORT_GROUP, direction = Direction.OUT)
    void addArchiveGroup(DependencyReportDependencyGroupModel groupModel);
}