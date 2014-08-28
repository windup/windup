package org.jboss.windup.reporting.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * This lists all of the top level reports within the application report (eg, Application classes report, Hibernate
 * entity report, EJB report, etc). Potential uses include navigation bars within a report.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
@TypeValue("ApplicationReportIndexModel")
public interface ApplicationReportIndexModel extends WindupVertexFrame
{
    public static final String APPLICATION_REPORT_INDEX_TO_PROJECT_MODEL = "appReportIndexToProjectModel";
    public static final String APPLICATION_REPORT_INDEX_TO_REPORT_MODEL = "appReportIndexToAppReportModel";

    /**
     * Get all ReportModels that should be displayed in the index in ascending order, according to priority
     */
    @JavaHandler
    public List<ApplicationReportModel> getApplicationReportModelsSortedByPriority();

    /**
     * Get all ReportModels that should be displayed in the index
     */
    @Adjacency(label = APPLICATION_REPORT_INDEX_TO_REPORT_MODEL, direction = Direction.OUT)
    public Iterable<ApplicationReportModel> getApplicationReportModels();

    /**
     * Adds a ReportModel that should be displayed in the index
     */
    @Adjacency(label = APPLICATION_REPORT_INDEX_TO_REPORT_MODEL, direction = Direction.OUT)
    public void addApplicationReportModel(ApplicationReportModel reportModel);

    /**
     * Associates a Set of ProjectModels with this index. This allows us to get from any Project Model to the associated
     * index.
     * 
     * NOTE: This should generally include the projectmodel and all child projects (flattened) to make searching easier.
     */
    @Adjacency(label = APPLICATION_REPORT_INDEX_TO_PROJECT_MODEL, direction = Direction.OUT)
    public Iterable<ProjectModel> getProjectModels();

    /**
     * Associates a Set of ProjectModels with this index. This allows us to get from any Project Model to the associated
     * index.
     * 
     * NOTE: This should generally include the projectmodel and all child projects (flattened) to make searching easier.
     */
    @Adjacency(label = APPLICATION_REPORT_INDEX_TO_PROJECT_MODEL, direction = Direction.OUT)
    public void addProjectModel(ProjectModel projectModel);

    abstract class Impl implements ApplicationReportIndexModel, JavaHandlerContext<Vertex>
    {
        public List<ApplicationReportModel> getApplicationReportModelsSortedByPriority()
        {
            List<ApplicationReportModel> reports = new ArrayList<>();
            for (ApplicationReportModel m : getApplicationReportModels())
            {
                reports.add(m);
            }

            Collections.sort(reports, new Comparator<ApplicationReportModel>()
            {
                @Override
                public int compare(ApplicationReportModel o1, ApplicationReportModel o2)
                {
                    return o1.getReportPriority() - o2.getReportPriority();
                }
            });
            return reports;
        }
    }
}
