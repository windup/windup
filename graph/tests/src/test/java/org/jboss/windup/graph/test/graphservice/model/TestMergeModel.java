package org.jboss.windup.graph.test.graphservice.model;

import org.jboss.windup.graph.Property;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@TypeValue("TestMerge")
public interface TestMergeModel extends WindupVertexFrame
{
    @Property("prop1")
    public void setProp1(String prop);

    @Property("prop1")
    public String getProp1();

    @Property("prop2")
    public TestMergeModel setProp2(String prop);

    @Property("prop2")
    public String getProp2();

    @Property("prop3")
    public TestMergeModel setProp3(String prop);

    @Property("prop3")
    public String getProp3();
}// class
