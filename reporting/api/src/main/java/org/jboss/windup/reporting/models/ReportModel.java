package org.jboss.windup.reporting.models;

import java.util.Map;

import org.jboss.windup.graph.AdjacentMap;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.ResourceModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("Report")
public interface ReportModel extends WindupVertexFrame
{
    /**
     * The name of the report (for example, 'Black List report for Foo.class')
     * 
     * @param reportName
     */
    @Property("reportName")
    public void setReportName(String reportName);

    @Property("reportName")
    public String getReportName();

    @Property("reportFilename")
    public void setReportFilename(String reportFilename);

    @Property("reportFilename")
    public String getReportFilename();

    /**
     * The path to the template that produced this report (for example, /reports/blacklist.ftl)
     * 
     * @param templatePath
     */
    @Property("templatePath")
    public void setTemplatePath(String templatePath);

    @Property("templatePath")
    public String getTemplatePath();

    /**
     * The templating technology used to produce this report (for example, freemarker)
     * 
     * @param templateType
     */
    @Property("templateType")
    public void setTemplateType(TemplateType templateType);

    @Property("templateType")
    public TemplateType getTemplateType();

    /**
     * Refers to the original source file (eg, the original .java file or original .xml file) represented by this report
     * entry.
     * 
     * @param resourceModel
     */
    @Property("sourceFileResource")
    public void setSourceFileResource(ResourceModel resourceModel);

    @Property("sourceFileResource")
    public ResourceModel getSourceFileResource();

    /**
     * The parent report... this could be the root (index) or another level of summary report.
     * 
     * @return
     */
    @Adjacency(label = "parentReport", direction = Direction.IN)
    public ReportModel getParentReport();

    @Adjacency(label = "parentReport", direction = Direction.IN)
    public void setParentReport(ReportModel parent);

    /**
     * The graph objects that took part in producing this report
     * 
     * @param wvf
     */
    @AdjacentMap(label = "relatedResources")
    public void addRelatedResource(Map<String, WindupVertexFrame> wvf);

    @AdjacentMap(label = "relatedResources")
    public Map<String, WindupVertexFrame> getRelatedResources();

    /**
     * Provides a list of child reports referenced by this report
     * 
     * @return
     */
    @Adjacency(label = "childReport", direction = Direction.OUT)
    public Iterable<ReportModel> getChildReports();

    @Adjacency(label = "childReport", direction = Direction.OUT)
    public void addChildReport(final ReportModel reportResource);
}
