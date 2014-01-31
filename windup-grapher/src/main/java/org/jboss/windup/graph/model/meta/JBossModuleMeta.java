package org.jboss.windup.graph.model.meta;

import java.util.Iterator;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("JBossModuleMeta")
public interface JBossModuleMeta extends Meta {

	@Property("moduleName")
	public String getModuleName();
	
	@Property("moduleName")
	public void setModuleName(String moduleName);
	
	@Property("slotName")
	public String getSlotName();
	
	@Property("slotName")
	public void setSlotName(String slotName);

	@Adjacency(label="depends", direction=Direction.OUT)
	public void addDependency(final JBossModuleMeta module);

	@Adjacency(label="implements", direction=Direction.OUT)
	public Iterator<JBossModuleMeta> getDependencies();

}
