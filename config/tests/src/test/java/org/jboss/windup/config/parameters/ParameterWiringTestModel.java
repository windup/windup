package org.jboss.windup.config.parameters;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
@TypeValue("parameterwiringmodel")
public interface ParameterWiringTestModel extends WindupVertexFrame
{
    String VALUE = "value";

    @Property(VALUE)
    String getValue();

    @Property(VALUE)
    void setValue(String name);
}
