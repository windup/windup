package org.jboss.windup.graph.typedgraph;

import org.jboss.windup.graph.frames.FrameBooleanDefaultValue;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.resource.ResourceModel;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.syncleus.ferma.annotations.Property;

/**
 * 
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
@TypeValue("Foo")
public interface TestFooModel extends ResourceModel
{
    @Property("prop1")
    TestFooModel setProp1(String prop);

    @Property("prop1")
    String getProp1();

    @Property("prop2")
    TestFooModel setProp2(String prop);

    @Property("prop2")
    String getProp2();

    @Property("prop3")
    TestFooModel setProp3(String prop);

    @Property("prop3")
    String getProp3();

    @Property("prop4")
    @FrameBooleanDefaultValue(false)
    void setProp4(Boolean prop);

    @Property("prop4")
    Boolean getProp4();


    default String testJavaMethod()
    {
        return "base";
    }
}
