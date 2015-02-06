package org.jboss.windup.graph.typedgraph;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
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

    @Override
    @JavaHandler
    public String testJavaMethod();

    abstract class Impl implements TestFooSubModel, JavaHandlerContext<Vertex>
    {
        public String testJavaMethod()
        {
            return "subclass";
        }
    }
}