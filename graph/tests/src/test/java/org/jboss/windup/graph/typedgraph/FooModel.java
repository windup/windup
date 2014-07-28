package org.jboss.windup.graph.typedgraph;

import org.jboss.windup.graph.model.resource.ResourceModel;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@TypeValue("Foo")
public interface FooModel extends ResourceModel
{
    @Property("prop1")
    public void setProp1(String prop);

    @Property("prop1")
    public String getProp1();

    @Property("prop2")
    public void setProp2(String prop);

    @Property("prop2")
    public String getProp2();

    @Property("prop3")
    public void setProp3(String prop);

    @Property("prop3")
    public String getProp3();
}// class
