package org.jboss.windup.graph.typedgraph;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@TypeValue("FooSub")
public interface TestFooSubModel extends TestFooModel
{

    @Property("fooProperty")
    public String getFoo();

    @Property("fooProperty")
    public void setFoo(String foo);
    
}// class
