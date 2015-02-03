package org.jboss.windup.reporting.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jboss.windup.graph.MapInAdjacentVertices;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Provides the base object for all reports.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@TypeValue("Report")
public interface ReportModel extends WindupVertexFrame
{
    public static final String CHILD_REPORT = "childReport";
    public static final String RELATED_RESOURCES = "relatedResources";
    public static final String PARENT_REPORT = "parentReport";
    public static final String TEMPLATE_TYPE = "templateType";
    public static final String TEMPLATE_PATH = "templatePath";
    public static final String REPORT_FILENAME = "reportFilename";
    public static final String REPORT_ICON_CLASS = "reportIconClass";
    public static final String REPORT_NAME = "reportName";
    public static final String REPORT_SUBDIRECTORY = "reportSubdirectory";

    /**
     * The name of the report (for example, 'ClassLoader Report' or 'EJB Report')
     */
    @Property(REPORT_NAME)
    public void setReportName(String reportName);

    /**
     * The name of the report (for example, 'ClassLoader Report' or 'EJB Report')
     */
    @Property(REPORT_NAME)
    public String getReportName();

    /**
     * The filename of the report on disk (useful for other reports that need to link to this one)
     */
    @Property(REPORT_FILENAME)
    public void setReportFilename(String reportFilename);

    /**
     * The filename of the report on disk (useful for other reports that need to link to this one)
     */
    @Property(REPORT_FILENAME)
    public String getReportFilename();

    /**
     * The path to the template that produced this report (for example, /reports/blacklist.ftl)
     */
    @Property(TEMPLATE_PATH)
    public void setTemplatePath(String templatePath);

    /**
     * The path to the template that produced this report (for example, /reports/blacklist.ftl)
     */
    @Property(TEMPLATE_PATH)
    public String getTemplatePath();

    /**
     * The templating technology used to produce this report (for example, freemarker)
     * 
     * @param templateType
     */
    @Property(TEMPLATE_TYPE)
    public void setTemplateType(TemplateType templateType);

    @Property(TEMPLATE_TYPE)
    public TemplateType getTemplateType();


    /**
     * CSS Class for the Report Icon
     * @return
     */
    @Property(REPORT_ICON_CLASS)
    public String getReportIconClass();

    /**
     * CSS Class for the Report Icon
     * @param reportName
     */
    @Property(REPORT_ICON_CLASS)
    public void setReportIconClass(String reportName);

    
    /**
     * The parent report... this could be the root (index) or another level of summary report.
     * 
     * @return
     */
    @Adjacency(label = PARENT_REPORT, direction = Direction.IN)
    public ReportModel getParentReport();

    @Adjacency(label = PARENT_REPORT, direction = Direction.IN)
    public void setParentReport(ReportModel parent);

    /**
     * The graph objects that took part in producing this report
     * 
     * @param wvf
     */
    @MapInAdjacentVertices(label = RELATED_RESOURCES)
    public void setRelatedResource(Map<String, WindupVertexFrame> wvf);

    @MapInAdjacentVertices(label = RELATED_RESOURCES)
    public Map<String, WindupVertexFrame> getRelatedResources();

    /**
     * Provides a list of child reports referenced by this report
     * 
     * @return
     */
    @Adjacency(label = CHILD_REPORT, direction = Direction.OUT)
    public Iterable<ReportModel> getChildReports();

    @Adjacency(label = CHILD_REPORT, direction = Direction.OUT)
    public void addChildReport(final ReportModel reportResource);

    /**
     * Get all ReportModels that should be displayed in the path to this report.
     */
    @JavaHandler
    public List<ReportModel> getAllParentsInReversedOrder();

    abstract class Impl implements ReportModel, JavaHandlerContext<Vertex>
    {
        public List<ReportModel> getAllParentsInReversedOrder()
        {
            List<ReportModel> reports = new ArrayList<>();
            ReportModel currentReport = this;
            reports.add(this);
            while (currentReport.getParentReport() != null)
            {
                reports.add(currentReport.getParentReport());
                currentReport = currentReport.getParentReport();
            }

            Collections.reverse(reports);
            return reports;
        }
    }
}
