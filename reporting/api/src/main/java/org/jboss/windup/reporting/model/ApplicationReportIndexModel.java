package org.jboss.windup.reporting.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;

/**
 * This lists all of the top level reports within the application report (eg, Application classes report, Hibernate
 * entity report, EJB report, etc). Potential uses include navigation bars within a report.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(ApplicationReportIndexModel.TYPE)
public interface ApplicationReportIndexModel extends WindupVertexFrame {
    String TYPE = "ApplicationReportIndexModel";
    String APPLICATION_REPORT_INDEX_TO_PROJECT_MODEL = "appReportIndexToProjectModel";
    String APPLICATION_REPORT_INDEX_TO_REPORT_MODEL = "appReportIndexToAppReportModel";

    /**
     * Get all ReportModels that should be displayed in the index in ascending order, according to priority
     */
    default List<ApplicationReportModel> getApplicationReportModelsSortedByPriority() {
        List<ApplicationReportModel> reports = new ArrayList<>();
        for (ApplicationReportModel m : getApplicationReportModels()) {
            reports.add(m);
        }

        Collections.sort(reports, new Comparator<ApplicationReportModel>() {
            @Override
            public int compare(ApplicationReportModel o1, ApplicationReportModel o2) {
                return o1.getReportPriority() - o2.getReportPriority();
            }
        });
        return reports;
    }

    /**
     * Get all ReportModels that should be displayed in the index
     */
    @Adjacency(label = APPLICATION_REPORT_INDEX_TO_REPORT_MODEL, direction = Direction.OUT)
    List<ApplicationReportModel> getApplicationReportModels();

    /**
     * Adds a ReportModel that should be displayed in the index
     */
    @Adjacency(label = APPLICATION_REPORT_INDEX_TO_REPORT_MODEL, direction = Direction.OUT)
    void addApplicationReportModel(ApplicationReportModel reportModel);

    /**
     * Associates a Set of ProjectModels with this index. This allows us to get from any Project Model to the associated
     * index.
     * <p>
     * NOTE: This should generally include the projectmodel and all child projects (flattened) to make searching easier.
     */
    @Adjacency(label = APPLICATION_REPORT_INDEX_TO_PROJECT_MODEL, direction = Direction.OUT)
    List<ProjectModel> getProjectModels();

    /**
     * Associates a Set of ProjectModels with this index. This allows us to get from any Project Model to the associated
     * index.
     * <p>
     * NOTE: This should generally include the projectmodel and all child projects (flattened) to make searching easier.
     */
    @Adjacency(label = APPLICATION_REPORT_INDEX_TO_PROJECT_MODEL, direction = Direction.OUT)
    void addProjectModel(ProjectModel projectModel);
}
