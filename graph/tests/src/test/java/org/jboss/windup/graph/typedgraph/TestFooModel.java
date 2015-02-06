package org.jboss.windup.graph.typedgraph;

import org.jboss.windup.graph.model.resource.ResourceModel;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@TypeValue("Foo")
public interface TestFooModel extends ResourceModel
{
    @Property("prop1")
    public TestFooModel setProp1(String prop);

    @Property("prop1")
    public String getProp1();

    @Property("prop2")
    public TestFooModel setProp2(String prop);

    @Property("prop2")
    public String getProp2();

    @Property("prop3")
    public TestFooModel setProp3(String prop);

    @Property("prop3")
    public String getProp3();

    @JavaHandler
    public String testJavaMethod();

    abstract class Impl implements TestFooModel, JavaHandlerContext<Vertex>
    {
        public String testJavaMethod()
        {
            return "base";
        }
    }
}