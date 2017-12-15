package org.jboss.windup.graph.typedgraph;

import org.jboss.windup.graph.frames.FrameBooleanDefaultValue;
import org.jboss.windup.graph.model.resource.ResourceModel;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.syncleus.ferma.annotations.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * 
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
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

    @Property("prop4")
    @FrameBooleanDefaultValue(false)
    public void setProp4(Boolean prop);

    @Property("prop4")
    public Boolean getProp4();

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
