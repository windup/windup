package org.jboss.windup.rules.apps.ejb.model;

import org.jboss.windup.rules.apps.java.scan.model.JarArchiveModel;
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
