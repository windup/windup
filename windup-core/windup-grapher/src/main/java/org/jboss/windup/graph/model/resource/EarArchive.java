package org.jboss.windup.graph.model.resource;

import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("EarArchiveResource")
public interface EarArchive extends JarArchive {

	@GremlinGroovy("it.out('child').has('type', 'JarArchiveResource')")
	public Iterable<JarArchive> getJars();

	@GremlinGroovy("it.out('child').has('type', 'WarArchiveResource')")
	public Iterable<WarArchive> getWars();

	@GremlinGroovy("it.out('child').has('type', 'EarArchiveResource')")
	public Iterable<EarArchive> getEars();
}
