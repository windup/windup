package org.jboss.windup.graph.model.resource;

import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("EarArchiveResource")
public interface EarArchiveModel extends JarArchiveModel {

	@GremlinGroovy("it.out('child').has('type', 'JarArchiveResource')")
	public Iterable<JarArchiveModel> getJars();

	@GremlinGroovy("it.out('child').has('type', 'WarArchiveResource')")
	public Iterable<WarArchiveModel> getWars();

	@GremlinGroovy("it.out('child').has('type', 'EarArchiveResource')")
	public Iterable<EarArchiveModel> getEars();
}
