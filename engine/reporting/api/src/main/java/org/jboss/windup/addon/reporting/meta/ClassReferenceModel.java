package org.jboss.windup.addon.reporting.meta;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("ClassReferenceModel")
public interface ClassReferenceModel extends ReportModel
{
    @Property("className")
    public String getClassName();
    
    @Property("className")
    public void setClassName(String className);

    @Property("referenceType")
    public String getReferenceType();
    
    @Property("referenceType")
    public void setReferenceType(String referenceType);

}
