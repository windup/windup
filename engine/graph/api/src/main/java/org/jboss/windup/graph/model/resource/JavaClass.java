package org.jboss.windup.graph.model.resource;

import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.annotations.gremlin.GremlinParam;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("JavaClassResource")
public interface JavaClass extends Resource
{

    @Adjacency(label = "javaClassFacet", direction = Direction.IN)
    public Iterable<Resource> getResources();

    @Adjacency(label = "javaClassFacet", direction = Direction.IN)
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

    @Adjacency(label = "imports", direction = Direction.OUT)
    public void addImport(final JavaClass javaImport);

    @Adjacency(label = "imports", direction = Direction.OUT)
    public Iterable<JavaClass> getImports();

    @Adjacency(label = "extends", direction = Direction.OUT)
    public JavaClass getExtends();

    @Adjacency(label = "extends", direction = Direction.OUT)
    public void setExtends(final JavaClass javaFacet);

    @Adjacency(label = "implements", direction = Direction.OUT)
    public void addImplements(final JavaClass javaFacet);

    @Adjacency(label = "implements", direction = Direction.OUT)
    public Iterable<JavaClass> getImplements();

    @GremlinGroovy("it.in('javaClassFacet').in('child').dedup")
    public Iterable<JarArchive> providedBy();

    @Adjacency(label = "source", direction = Direction.OUT)
    public void setSource(FileResource source);

    @Adjacency(label = "source", direction = Direction.OUT)
    public FileResource getSource();

    @GremlinGroovy("it.out('javaMethod').has('methodName', methodName)")
    public Iterable<JavaMethod> getMethod(@GremlinParam("methodName") String methodName);

    @Adjacency(label = "javaMethod", direction = Direction.OUT)
    public void addJavaMethod(final JavaMethod javaMethod);

    @Adjacency(label = "javaMethod", direction = Direction.OUT)
    public Iterable<JavaMethod> getJavaMethods();

    @GremlinGroovy("it.in('javaClassFacet').in('childArchiveEntry')")
    public Iterable<ArchiveResource> getArchivesProvidingClass();

    @GremlinGroovy("it.sideEffect{x=it}.out('extends', 'imports', 'implements').dedup().filter{it!=x}")
    public Iterable<JavaClass> dependsOnJavaClass();

    @GremlinGroovy("it.sideEffect{x=it}.in('extends', 'imports', 'implements').dedup().filter{it!=x}")
    public Iterable<JavaClass> providesForJavaClass();

    @GremlinGroovy(value = "it.has('blacklistCandidate').hasNext()", frame = false)
    public boolean isBlacklistCandidate();

    @GremlinGroovy(value = "it.has('customerPackage').hasNext()", frame = false)
    public boolean isCustomerPackage();

}
