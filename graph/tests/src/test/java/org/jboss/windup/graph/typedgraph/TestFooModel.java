package org.jboss.windup.graph.typedgraph;

import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.frames.FrameBooleanDefaultValue;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.resource.ResourceModel;

/**
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
@TypeValue("Foo")
public interface TestFooModel extends ResourceModel {
    @Property("prop1")
    String getProp1();

    @Property("prop1")
    void setProp1(String prop);

    @Property("prop2")
    String getProp2();

    @Property("prop2")
    void setProp2(String prop);

    @Property("prop3")
    String getProp3();

    @Property("prop3")
    void setProp3(String prop);

    @Property("prop4")
    Boolean getProp4();

    @Property("prop4")
    @FrameBooleanDefaultValue(false)
    void setProp4(Boolean prop);

    default String testJavaMethod() {
        return "base";
    }
}
