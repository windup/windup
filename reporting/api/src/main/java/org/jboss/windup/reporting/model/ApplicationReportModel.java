package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.model.ProjectModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * These reports are directly associated with an application, and that application's project model. These can include things like an Application
 * Overview report (with various hints, etc) as well as more specific reports (hibernate reports, ejb reports, classloading reports, etc).
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@TypeValue(ApplicationReportModel.TYPE)
public interface ApplicationReportModel extends ReportModel
{
    public static final String DISPLAY_IN_APPLICATION_REPORT_INDEX = "displayInApplicationReportIndex";
    public static final String DISPLAY_IN_APPLICATION_LIST = "displayInApplicationList";
    public static final String TYPE = "ApplicationReport";
    public static final String REPORT_TO_APPLICATION_NOTE = "reportToApplicationNote";
    public static final String REPORT_LINES = "reportLines";
    public static final String REPORT_TO_PROJECT_MODEL = "reportToProjectModel";
    public static final String REPORT_PRIORITY = "reportPriority";
    public static final String MAIN_APPLICATION_REPORT = "mainApplicationModel";

    /**
     * Provides a link to the Navigation Index that is used for this particular report
     */
    @Adjacency(label = ApplicationReportIndexModel.APPLICATION_REPORT_INDEX_TO_REPORT_MODEL, direction = Direction.IN)
    public void setApplicationReportIndexModel(ApplicationReportIndexModel navIndex);

    /**
     * Provides a link to the Navigation Index that is used for this particular report
     */
    @Adjacency(label = ApplicationReportIndexModel.APPLICATION_REPORT_INDEX_TO_REPORT_MODEL, direction = Direction.IN)
    public ApplicationReportIndexModel getApplicationReportIndexModel();

    /**
     * This can be used to determine a reports location in a navigation bar. The primary purpose is sorting.
     */
    @Property(REPORT_PRIORITY)
    public int getReportPriority();

    /**
     * This can be used to determine a reports location in a navigation bar. The primary purpose is sorting.
     */
    @Property(REPORT_PRIORITY)
    public void setReportPriority(int priority);

    /**
     * Indicates whether or not to display this in the list of all applications. Usually this would be true for a main "overview" type report for a
     * particular application, and false for everything else.
     */
    @Property(DISPLAY_IN_APPLICATION_LIST)
    public Boolean getDisplayInApplicationList();

    /**
     * Indicates whether or not to display this in the list of all applications. Usually this would be true for a main "overview" type report for a
     * particular application, and false for everything else.
     */
    @Property(DISPLAY_IN_APPLICATION_LIST)
    public void setDisplayInApplicationList(Boolean displayInApplicationList);

    /**
     * Indicates whether to display this report in the navigation index for the current application.
     */
    @Property(DISPLAY_IN_APPLICATION_REPORT_INDEX)
    public Boolean getDisplayInApplicationReportIndex();

    /**
     * Indicates whether to display this report in the navigation index for the current application.
     */
    @Property(DISPLAY_IN_APPLICATION_REPORT_INDEX)
    public void setDisplayInApplicationReportIndex(Boolean displayInIndex);

    /**
     * Indicates whether or not this is the main report for the application.
     */
    @Property(MAIN_APPLICATION_REPORT)
    public Boolean isMainApplicationReport();

    /**
     * Indicates whether or not this is the main report for the application.
     */
    @Property(MAIN_APPLICATION_REPORT)
    public void setMainApplicationReport(Boolean mainApplicationReport);

    
    /**
     * Application notes allow custom text to be added
     */
    @Adjacency(label = REPORT_LINES, direction = Direction.OUT)
    public Iterable<OverviewReportLineMessageModel> getApplicationReportLines();

    /**
     * Application notes allow custom text to be added
     */
    @Adjacency(label = REPORT_LINES, direction = Direction.OUT)
    public void addApplicationReportLine(OverviewReportLineMessageModel line);
    
    
    /**
     * Application notes allow custom text to be added
     */
    @Adjacency(label = REPORT_TO_APPLICATION_NOTE, direction = Direction.OUT)
    public Iterable<String> getApplicationNotes();

    /**
     * Application notes allow custom text to be added
     */
    @Adjacency(label = REPORT_TO_APPLICATION_NOTE, direction = Direction.OUT)
    public void addApplicationNote(String applicationNote);

    /**
     * The ProjectModel associated with this Application Report.
     */
    @Adjacency(label = REPORT_TO_PROJECT_MODEL, direction = Direction.OUT)
    public ProjectModel getProjectModel();

    /**
     * The ProjectModel associated with this Application Report.
     */
    @Adjacency(label = REPORT_TO_PROJECT_MODEL, direction = Direction.OUT)
    public void setProjectModel(ProjectModel projectModel);

}
