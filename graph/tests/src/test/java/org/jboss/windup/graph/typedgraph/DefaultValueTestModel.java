package org.jboss.windup.graph.typedgraph;

import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.frames.FrameBooleanDefaultValue;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * A vertex used to test default value settings
 *
 * @author <a href="mailto:mbriskar@redhat.com">Matej Briskar</a>
 */
@TypeValue("DefaultValueTestModel")
public interface DefaultValueTestModel extends WindupVertexFrame {

    @Property("defaultFalse")
    public Boolean getDefaultFalseValue();

    @Property("defaultFalse")
    @FrameBooleanDefaultValue(false)
    public void setDefaultFalseValue(Boolean prop);

    @Property("defaultTrue")
    public Boolean getDefaultTrueValue();

    @Property("defaultTrue")
    @FrameBooleanDefaultValue(true)
    public void setDefaultTrueValue(Boolean prop);


}
