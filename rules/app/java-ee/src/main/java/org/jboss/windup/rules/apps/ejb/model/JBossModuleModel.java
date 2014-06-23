package org.jboss.windup.rules.apps.ejb.model;

import java.util.Iterator;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

@TypeValue("JBossModuleMeta")
public interface JBossModuleModel extends WindupVertexFrame
{

    @Property("moduleName")
    public String getModuleName();

    @Property("moduleName")
    public void setModuleName(String moduleName);

    @Property("slotName")
    public String getSlotName();

    @Property("slotName")
    public void setSlotName(String slotName);

    @Adjacency(label = "depends", direction = Direction.OUT)
    public void addDependency(final JBossModuleModel module);

    @Adjacency(label = "implements", direction = Direction.OUT)
    public Iterator<JBossModuleModel> getDependencies();

}
