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
 * entity report, EJB report, etc).
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
@TypeValue("MainNavigationIndexModel")
public interface MainNavigationIndexModel extends WindupVertexFrame
{
    public static final String NAVIGATION_INDEX_TO_PROJECT_MODEL = "navigationIndexToProjectModel";
    public static final String NAVIGATION_INDEX_TO_REPORT_MODEL = "navigationIndexToReportModel";

    /**
     * Get all ReportModels that should be displayed in the index in ascending order, according to priority
     */
    @JavaHandler
    public List<ReportModel> getReportModelsSortedByPriority();

    /**
     * Get all ReportModels that should be displayed in the index
     */
    @Adjacency(label = NAVIGATION_INDEX_TO_REPORT_MODEL, direction = Direction.OUT)
    public Iterable<ReportModel> getReportModels();

    /**
     * Adds a ReportModel that should be displayed in the index
     */
    @Adjacency(label = NAVIGATION_INDEX_TO_REPORT_MODEL, direction = Direction.OUT)
    public void addReportModel(ReportModel reportModel);

    /**
     * Associates a Set of ProjectModels with this index. This allows us to get from any Project Model to the associated
     * index.
     * 
     * NOTE: This should generally include the projectmodel and all child projects (flattened) to make searching easier.
     */
    @Adjacency(label = NAVIGATION_INDEX_TO_PROJECT_MODEL, direction = Direction.OUT)
    public Iterable<ProjectModel> getProjectModels();

    /**
     * Associates a Set of ProjectModels with this index. This allows us to get from any Project Model to the associated
     * index.
     * 
     * NOTE: This should generally include the projectmodel and all child projects (flattened) to make searching easier.
     */
    @Adjacency(label = NAVIGATION_INDEX_TO_PROJECT_MODEL, direction = Direction.OUT)
    public void addProjectModel(ProjectModel projectModel);

    abstract class Impl implements MainNavigationIndexModel, JavaHandlerContext<Vertex>
    {
        public List<ReportModel> getReportModelsSortedByPriority()
        {
            List<ReportModel> reports = new ArrayList<>();
            for (ReportModel m : getReportModels())
            {
                reports.add(m);
            }

            Collections.sort(reports, new Comparator<ReportModel>()
            {
                @Override
                public int compare(ReportModel o1, ReportModel o2)
                {
                    return o1.getReportPriority() - o2.getReportPriority();
                }
            });
            return reports;
        }
    }
}
