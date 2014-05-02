package org.jboss.windup.addon.reporting.meta;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("ClassloaderReport")
public interface ClassLoaderReportRowModel extends ReportModel
{
    @Property("className")
    public String getClassName();
    
    @Property("className")
    public void setClassName(String className);

    @Adjacency(label="classReference", direction=Direction.OUT)
    public Iterable<ClassReferenceModel> getClassLoaderReportRow();
    
    @Adjacency(label="classReference", direction=Direction.OUT)
    public void addClassLoaderReportRow(ClassReferenceModel classLoaderReportRow);
}
