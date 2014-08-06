package org.jboss.windup.reporting.meta.test.model;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.reporting.meta.ann.Description;

/**
 * This should inherit all the report related annotations from the parent.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@TypeValue("ReportCommonsTestSub")
public interface ReportCommonsTestElementSubModel extends ReportCommonsTestElementModel
{

    @Description
    @Property("desc") String getDesc();
    @Property("desc") String setDesc();
    
}// class
