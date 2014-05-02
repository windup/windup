package org.jboss.windup.addon.reporting.meta;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("ApplicationReport")
public interface ApplicationReportModel extends ReportModel
{
    @Property("applicationName")
    public String getApplicationName();
    
    @Property("applicationName")
    public void setApplicationName(String applicationName);
    
    @Property("applicationVersion")
    public String getApplicationVersion();
    
    @Property("applicationVersion")
    public String setApplicationVersion(String applicationVersion);
    
    @Property("applicationCreator")
    public String getApplicationCreator();
    
    @Property("applicationCreator")
    public String setApplicationCreator(String applicationCreator);
    
    /**
     * Application notes allow custom text to be added 
     * 
     * @return
     */
    @Adjacency(label="applicationNote", direction=Direction.OUT)
    public Iterable<String> getApplicationNotes();
    
    @Adjacency(label="applicationNote", direction=Direction.OUT)
    public void addApplicationNote(String applicationNote);
    
    @Adjacency(label="childReport", direction=Direction.OUT)
    public Iterable<ReportModel> getChildReports();
    
    @Adjacency(label="childReport", direction=Direction.OUT)
    public void addChildReport(final ReportModel reportResource);

}
