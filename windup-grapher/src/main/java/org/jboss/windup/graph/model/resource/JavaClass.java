package org.jboss.windup.graph.model.resource;

import java.util.Iterator;

import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("JavaClassResource")
public interface JavaClass extends File {

	@Label
	@Property("qualifiedName")
	public String getQualifiedName();

	@Property("qualifiedName")
	public String setQualifiedName(String qualifiedName);

	@Adjacency(label="imports", direction=Direction.OUT)
	public void addImport(final JavaClass person);

	@Adjacency(label="imports", direction=Direction.OUT)
	public Iterator<JavaClass> getImports(final JavaClass person);

	@Adjacency(label="extends", direction=Direction.OUT)
	public Iterator<JavaClass> getExtends(final JavaClass person);

	@Adjacency(label="extends", direction=Direction.OUT)
	public Iterator<JavaClass> addExtends(final JavaClass person);

	@Adjacency(label="implements", direction=Direction.OUT)
	public Iterator<JavaClass> addImplements(final JavaClass person);

	@Adjacency(label="implements", direction=Direction.OUT)
	public Iterator<JavaClass> getImplements(final JavaClass person);

}
