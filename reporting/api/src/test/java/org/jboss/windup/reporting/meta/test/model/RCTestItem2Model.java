package org.jboss.windup.reporting.meta.test.model;

import com.tinkerpop.frames.Property;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public interface RCTestItem2Model extends WindupVertexFrame {
    
    @Property("name") String getName();
    @Property("name") String setName();

}// class
