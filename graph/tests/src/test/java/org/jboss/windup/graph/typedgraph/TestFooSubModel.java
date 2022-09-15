package org.jboss.windup.graph.typedgraph;

import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;

/**
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
@TypeValue("FooSub")
public interface TestFooSubModel extends TestFooModel {

    @Property("fooProperty")
    String getFoo();

    @Property("fooProperty")
    void setFoo(String foo);

    @Override
    default String testJavaMethod() {
        return "subclass";
    }
}
