package org.jboss.windup.graph.model;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("DatasourceMeta")
public interface DatasourceMetaModel extends WindupVertexFrame
{

    @Property("jndiName")
    public String getJndiName();

    @Property("jndiName")
    public void setJndiName(String jndiName);

}
