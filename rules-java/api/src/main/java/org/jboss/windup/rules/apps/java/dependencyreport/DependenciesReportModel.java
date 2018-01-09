package org.jboss.windup.rules.apps.java.dependencyreport;

import org.jboss.windup.reporting.model.ApplicationReportModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import com.syncleus.ferma.annotations.Adjacency;
import org.jboss.windup.graph.model.TypeValue;

/**
 * This represents Jar Dependencies report model
 * 
 * @author mnovotny
 *
 */
@TypeValue(DependenciesReportModel.TYPE)
public interface DependenciesReportModel extends ApplicationReportModel
{
    String TYPE = "DependenciesReportModel";

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
