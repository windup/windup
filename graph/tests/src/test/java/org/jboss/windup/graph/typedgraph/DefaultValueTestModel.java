package org.jboss.windup.graph.typedgraph;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.frames.FrameBooleanDefaultValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 *  A vertex used to test default value settings
 * @author <a href="mailto:mbriskar@redhat.com">Matej Briskar</a>
 */
@TypeValue("DefaultValueTestModel")
public interface DefaultValueTestModel extends WindupVertexFrame
{

    @Property("defaultFalse")
    @FrameBooleanDefaultValue(false)
    public void setDefaultFalseValue(Boolean prop);

    @Property("defaultFalse")
    public Boolean getDefaultFalseValue();

    @Property("defaultTrue")
    @FrameBooleanDefaultValue(true)
    public void setDefaultTrueValue(Boolean prop);

    @Property("defaultTrue")
    public Boolean getDefaultTrueValue();


}