package org.jboss.windup.graph.model.resource;

import java.util.Iterator;

import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("JavaClassResource")
public interface JavaClass extends Resource {

	@Adjacency(label="javaClassFacet", direction=Direction.IN)
	public Iterable<Resource> getResources();

	@Adjacency(label="javaClassFacet", direction=Direction.IN)
	public void addResource(Resource resource);

	@Label
	@Property("qualifiedName")
	public String getQualifiedName();

	@Property("qualifiedName")
	public String setQualifiedName(String qualifiedName);

	@Adjacency(label="imports", direction=Direction.OUT)
	public void addImport(final JavaClass person);

	@Adjacency(label="imports", direction=Direction.OUT)
	public Iterator<JavaClass> getImports(final JavaClass javaFacet);

	@Adjacency(label="extends", direction=Direction.OUT)
	public JavaClass getExtends();

	@Adjacency(label="extends", direction=Direction.OUT)
	public void setExtends(final JavaClass javaFacet);

	@Adjacency(label="implements", direction=Direction.OUT)
	public Iterator<JavaClass> addImplements(final JavaClass javaFacet);

	@Adjacency(label="implements", direction=Direction.OUT)
	public Iterator<JavaClass> getImplements();

}
