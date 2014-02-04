package org.jboss.windup.graph.model.resource;

import java.util.Iterator;

import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("JarArchiveResource")
public interface JarArchive extends Archive {

	@GremlinGroovy("it.as('x').out('child').filter{'type','JavaClassResource'}.select")
	public Iterator<JavaClass> getJavaClasses();
	
	@GremlinGroovy("it.as('x').out('child').filter{'type','XmlResource'}.select")
	public Iterator<XmlResource> getXmlFiles();
	
	
}
