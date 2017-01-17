package org.jboss.windup.rules.apps.javaee.model.stats;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
@TypeValue(TechnologyKeyValuePairModel.TYPE)
public interface TechnologyKeyValuePairModel extends WindupVertexFrame
{
    String TYPE = "TechnologyKeyValuePairModel";

    String NAME = "name";

    @Property(NAME)
    String getName();

    @Property(NAME)
    TechnologyKeyValuePairModel setName(String name);

    String VALUE = "value";

    @Property(VALUE)
    Integer getValue();

    @Property(VALUE)
    TechnologyKeyValuePairModel setValue(Integer value);
}
