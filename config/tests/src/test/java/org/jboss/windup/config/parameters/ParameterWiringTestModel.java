package org.jboss.windup.config.parameters;

import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@TypeValue("parameterwiringmodel")
public interface ParameterWiringTestModel extends WindupVertexFrame {
    String VALUE = "value";

    @Property(VALUE)
    String getValue();

    @Property(VALUE)
    void setValue(String name);
}
