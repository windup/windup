package org.jboss.windup.graph.model;

import java.util.Iterator;

import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("JavaClassResource")
public interface JavaClassResource extends FileResource {

	@Label
	@Property("qualifiedName")
	public String getQualifiedName();

	@Property("qualifiedName")
	public String setQualifiedName(String qualifiedName);

	@Adjacency(label="imports", direction=Direction.OUT)
	public void addImport(final JavaClassResource person);

	@Adjacency(label="imports", direction=Direction.OUT)
	public Iterator<JavaClassResource> getImports(final JavaClassResource person);

	@Adjacency(label="extends", direction=Direction.OUT)
	public Iterator<JavaClassResource> getExtends(final JavaClassResource person);

	@Adjacency(label="extends", direction=Direction.OUT)
	public Iterator<JavaClassResource> addExtends(final JavaClassResource person);

	@Adjacency(label="implements", direction=Direction.OUT)
	public Iterator<JavaClassResource> addImplements(final JavaClassResource person);

	@Adjacency(label="implements", direction=Direction.OUT)
	public Iterator<JavaClassResource> getImplements(final JavaClassResource person);

}
