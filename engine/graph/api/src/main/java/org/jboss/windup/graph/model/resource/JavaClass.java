package org.jboss.windup.graph.model.resource;

import java.util.Iterator;

import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
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
	public void setQualifiedName(String qualifiedName);

	@Property("packageName")
	public String getPackageName();

	@Property("packageName")
	public void setPackageName(String packageName);

	
	@Property("majorVersion")
	public int getMajorVersion();

	@Property("majorVersion")
	public void setMajorVersion(int majorVersion);	

	@Property("minorVersion")
	public int getMinorVersion();

	@Property("minorVersion")
	public void setMinorVersion(int minorVersion);	
	
	@Adjacency(label="imports", direction=Direction.OUT)
	public void addImport(final JavaClass javaImport);

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

	@GremlinGroovy("it.in('javaClassFacet').in('child').dedup")
	public Iterator<JarArchive> providedBy();
}
