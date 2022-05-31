package org.jboss.windup.reporting.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.MapInAdjacentVertices;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Provides the base object for all reports.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(ReportModel.TYPE)
public interface ReportModel extends WindupVertexFrame {
    String TYPE = "ReportModel";
    String CHILD_REPORT = "childReport";
    String RELATED_RESOURCES = "relatedResources";
    String PARENT_REPORT = "parentReport";
    String TEMPLATE_TYPE = "templateType";
    String TEMPLATE_PATH = "templatePath";
    String REPORT_FILENAME = "reportFilename";
    String REPORT_ICON_CLASS = "reportIconClass";
    String REPORT_NAME = "reportName";
    String DESCRIPTION = "reportDescription";

    /**
     * The name of the report (for example, 'ClassLoader Report' or 'EJB Report')
     */
    @Property(REPORT_NAME)
    String getReportName();

    /**
     * The name of the report (for example, 'ClassLoader Report' or 'EJB Report')
     */
    @Property(REPORT_NAME)
    void setReportName(String reportName);

    /**
     * Contains a full-text description of the report.
     */
    @Property(DESCRIPTION)
    String getDescription();

    /**
     * Contains a full-text description of the report.
     */
    @Property(DESCRIPTION)
    void setDescription(String description);

    /**
     * The filename of the report on disk (useful for other reports that need to link to this one)
     */
    @Property(REPORT_FILENAME)
    String getReportFilename();

    /**
     * The filename of the report on disk (useful for other reports that need to link to this one)
     */
    @Property(REPORT_FILENAME)
    void setReportFilename(String reportFilename);

    /**
     * The path to the template that produced this report (for example, /reports/blacklist.ftl)
     */
    @Property(TEMPLATE_PATH)
    String getTemplatePath();

    /**
     * The path to the template that produced this report (for example, /reports/blacklist.ftl)
     */
    @Indexed
    @Property(TEMPLATE_PATH)
    void setTemplatePath(String templatePath);

    @Property(TEMPLATE_TYPE)
    TemplateType getTemplateType();

    /**
     * The templating technology used to produce this report (for example, freemarker)
     *
     * @param templateType
     */
    @Property(TEMPLATE_TYPE)
    void setTemplateType(TemplateType templateType);

    /**
     * CSS Class for the Report Icon
     *
     * @return
     */
    @Property(REPORT_ICON_CLASS)
    String getReportIconClass();

    /**
     * CSS Class for the Report Icon
     *
     * @param reportName
     */
    @Property(REPORT_ICON_CLASS)
    void setReportIconClass(String reportName);


    /**
     * The parent report... this could be the root (index) or another level of summary report.
     */
    @Adjacency(label = PARENT_REPORT, direction = Direction.IN)
    ReportModel getParentReport();

    @Adjacency(label = PARENT_REPORT, direction = Direction.IN)
    void setParentReport(ReportModel parent);

    /**
     * The graph objects that took part in producing this report
     */
    @MapInAdjacentVertices(label = RELATED_RESOURCES)
    void setRelatedResource(Map<String, WindupVertexFrame> wvf);

    @MapInAdjacentVertices(label = RELATED_RESOURCES)
    Map<String, WindupVertexFrame> getRelatedResources();

    /**
     * Provides a list of child reports referenced by this report
     */
    @Adjacency(label = CHILD_REPORT, direction = Direction.OUT)
    List<ReportModel> getChildReports();

    @Adjacency(label = CHILD_REPORT, direction = Direction.OUT)
    void addChildReport(final ReportModel reportResource);

    /**
     * Get all ReportModels that should be displayed in the path to this report.
     */
    default List<ReportModel> getAllParentsInReversedOrder() {
        List<ReportModel> reports = new ArrayList<>();
        ReportModel currentReport = this;
        reports.add(this);
        while (currentReport.getParentReport() != null) {
            reports.add(currentReport.getParentReport());
            currentReport = currentReport.getParentReport();
        }

        Collections.reverse(reports);
        return reports;
    }
}
