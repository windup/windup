package org.jboss.windup.graph.model.meta;

import java.util.Set;

import org.jboss.windup.graph.model.resource.JarArchive;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("JarManifestMeta")
public interface JarManifest extends Meta {
	
	@Adjacency(label="meta", direction=Direction.IN)
	public void setJarArchive(final JarArchive archive);

	@Adjacency(label="meta", direction=Direction.IN)
	public JarArchive getJarArchive();

	
	@JavaHandler
	public String getProperty(String property);
	
	@JavaHandler
	public void setProperty(String propertyName, String obj);
	
	@JavaHandler
	public Set<String> keySet();
	
	
	abstract class Impl implements JarManifest, JavaHandlerContext<Vertex> {
		
		@Override
		public String getProperty(String property) {
			return this.it().getProperty(property);
		}
		
		@Override
		public void setProperty(String propertyName, String obj) {
			this.it().setProperty(propertyName, obj);
		}
		
		@Override
		public Set<String> keySet() {
			return this.it().getPropertyKeys();
		}
		
	}
	 
}
