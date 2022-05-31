package org.jboss.windup.reporting.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.MapInAdjacentProperties;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.TypeValue;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * These reports are directly associated with an application, and that application's project model. These can include things like an Application
 * Overview report (with various hints, etc) as well as more specific reports (hibernate reports, ejb reports, classloading reports, etc).
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(ApplicationReportModel.TYPE)
public interface ApplicationReportModel extends ReportModel {
    String DISPLAY_IN_APPLICATION_REPORT_INDEX = "displayInApplicationReportIndex";
    String DISPLAY_IN_GLOBAL_APPLICATION_INDEX = "displayInGlobalApplicationIndex";
    String TYPE = "ApplicationReportModel";
    String REPORT_LINES = "reportLines";
    String REPORT_TO_PROJECT_MODEL = "reportToProjectModel";
    String REPORT_PRIORITY = "reportPriority";
    String MAIN_APPLICATION_REPORT = "mainApplicationModel";
    String EXPORT_ALL_ISSUES_CSV = "exportAllIssuesCSV";

    /**
     * Provides a link to the Navigation Index that is used for this particular report. If there is more than one (for example, in the case of a
     * single report used both globally and associated with an application), then return the one associated with an app.
     */
    default ApplicationReportIndexModel getApplicationReportIndexModel() {
        ApplicationReportIndexModel result = null;
        Iterator<Vertex> vertexIterator = getElement().vertices(Direction.IN, ApplicationReportIndexModel.APPLICATION_REPORT_INDEX_TO_REPORT_MODEL);
        while (vertexIterator.hasNext()) {
            Vertex v = vertexIterator.next();
            ApplicationReportIndexModel model = getGraph().frameElement(v, ApplicationReportIndexModel.class);
            if (result == null)
                result = model;
            else if (!result.getProjectModels().iterator().hasNext() && model.getProjectModels().iterator().hasNext())
                result = model;
        }
        return result;
    }

    /**
     * Provides a link to the Navigation Index that is used for this particular report
     */
    @Adjacency(label = ApplicationReportIndexModel.APPLICATION_REPORT_INDEX_TO_REPORT_MODEL, direction = Direction.IN)
    void setApplicationReportIndexModel(ApplicationReportIndexModel navIndex);

    /**
     * This can be used to determine a reports location in a navigation bar. The primary purpose is sorting.
     */
    @Property(REPORT_PRIORITY)
    int getReportPriority();

    /**
     * This can be used to determine a reports location in a navigation bar. The primary purpose is sorting.
     */
    @Property(REPORT_PRIORITY)
    void setReportPriority(int priority);

    /**
     * Indicates that this report should also be attached to the global application index.
     */
    @Property(DISPLAY_IN_GLOBAL_APPLICATION_INDEX)
    Boolean getDisplayInGlobalApplicationIndex();

    /**
     * Indicates that this report should also be attached to the global application index.
     */
    @Property(DISPLAY_IN_GLOBAL_APPLICATION_INDEX)
    void setDisplayInGlobalApplicationIndex(Boolean displayInGlobalApplicationIndex);

    /**
     * Indicates whether to display this report in the navigation index for the current application. Examples are migration issues, EJBs ...
     */
    @Property(DISPLAY_IN_APPLICATION_REPORT_INDEX)
    Boolean getDisplayInApplicationReportIndex();

    /**
     * Indicates whether to display this report in the navigation index for the current application. Examples are migration issues, EJBs ...
     */
    @Property(DISPLAY_IN_APPLICATION_REPORT_INDEX)
    void setDisplayInApplicationReportIndex(Boolean displayInIndex);

    /**
     * Indicates whether or not this is the main report for the application. This boolean flag means this report will be referenced from all the
     * filemodels within application. Only one report per application must have this set to true.
     */
    @Property(MAIN_APPLICATION_REPORT)
    Boolean isMainApplicationReport();

    /**
     * Indicates whether or not this is the main report for the application. This boolean flag means this report will be referenced from all the
     * filemodels within application. Only one report per application must have this set to true.
     */
    @Property(MAIN_APPLICATION_REPORT)
    void setMainApplicationReport(Boolean mainApplicationReport);

    /**
     * Application notes allow custom text to be added
     */
    @Adjacency(label = REPORT_LINES, direction = Direction.OUT)
    List<OverviewReportLineMessageModel> getApplicationReportLines();

    /**
     * Application notes allow custom text to be added
     */
    @Adjacency(label = REPORT_LINES, direction = Direction.OUT)
    void addApplicationReportLine(OverviewReportLineMessageModel line);

    /**
     * The ProjectModel associated with this Application Report.
     */
    @Adjacency(label = REPORT_TO_PROJECT_MODEL, direction = Direction.OUT)
    ProjectModel getProjectModelNotNullSafe();

    default ProjectModel getProjectModel() {
        try {
            return getProjectModelNotNullSafe();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * The ProjectModel associated with this Application Report.
     */
    @Adjacency(label = REPORT_TO_PROJECT_MODEL, direction = Direction.OUT)
    void setProjectModel(ProjectModel projectModel);

    /**
     * Contains report properties
     */
    @MapInAdjacentProperties(label = "reportProperties")
    Map<String, String> getReportProperties();

    /**
     * Contains report properties
     */
    @MapInAdjacentProperties(label = "reportProperties")
    void setReportProperties(Map<String, String> map);

    /**
     * Flag for whether exporting a merged csv file containing issues from all applications is enabled
     */
    @Property(EXPORT_ALL_ISSUES_CSV)
    Boolean getIsExportAllIssuesCSV();

    /**
     * Flag for whether exporting a merged csv file containing issues from all applications is enabled
     */
    @Property(EXPORT_ALL_ISSUES_CSV)
    void setExportAllIssuesCSV(Boolean isExportAllIssuesCSV);
}
