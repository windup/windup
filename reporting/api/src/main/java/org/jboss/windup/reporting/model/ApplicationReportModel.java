package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.model.ProjectModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * 
 * These reports are directly associated with an application, and that application's project model.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@TypeValue("ApplicationReport")
public interface ApplicationReportModel extends ReportModel
{
    public static final String REPORT_TO_APPLICATION_NOTE = "reportToApplicationNote";
    public static final String REPORT_TO_PROJECT_MODEL = "reportToProjectModel";

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
