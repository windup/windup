package org.jboss.windup.graph.model.resource.facet;

import java.util.Iterator;

import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("JavaClassFacet")
public interface JavaClassFacet extends ResourceFacet {

	@Label
	@Property("qualifiedName")
	public String getQualifiedName();

	@Property("qualifiedName")
	public String setQualifiedName(String qualifiedName);

	@Adjacency(label="imports", direction=Direction.OUT)
	public void addImport(final JavaClassFacet person);

	@Adjacency(label="imports", direction=Direction.OUT)
	public Iterator<JavaClassFacet> getImports(final JavaClassFacet javaFacet);

	@Adjacency(label="extends", direction=Direction.OUT)
	public JavaClassFacet getExtends();

	@Adjacency(label="extends", direction=Direction.OUT)
	public void setExtends(final JavaClassFacet javaFacet);

	@Adjacency(label="implements", direction=Direction.OUT)
	public Iterator<JavaClassFacet> addImplements(final JavaClassFacet javaFacet);

	@Adjacency(label="implements", direction=Direction.OUT)
	public Iterator<JavaClassFacet> getImplements();

}
