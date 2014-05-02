package org.jboss.windup.addon.reporting.meta;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("ClassloaderReport")
public interface ClassLoaderReportModel extends ReportModel
{
    @Property("type")
    public String getType();
    
    @Property("type")
    public void setType(String type);
    
    @Property("referencedFrom")
    public String getReferencedFrom();
    
    @Property("referencedFrom")
    public String setReferencedFrom(String referencedFrom);
    
    @Property("referencedType")
    public String getReferencedType();
    
    @Property("referencedType")
    public String setReferencedType(String referencedType);
    
    @Adjacency(label="classLoaderReportRow", direction=Direction.OUT)
    public Iterable<ClassLoaderReportRowModel> getClassLoaderReportRow();
    
    @Adjacency(label="classLoaderReportRow", direction=Direction.OUT)
    public void addClassLoaderReportRow(ClassLoaderReportRowModel classLoaderReportRow);
    
}
