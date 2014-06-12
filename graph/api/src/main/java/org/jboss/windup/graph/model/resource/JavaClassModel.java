package org.jboss.windup.graph.model.resource;

import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.renderer.Label;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.annotations.gremlin.GremlinParam;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

// TODO:  WINDUP-85 - Introduce JavaFileModel
@TypeValue("JavaClassResource")
public interface JavaClassModel extends FileModel
{
    public static final String PROPERTY_QUALIFIED_NAME = "qualifiedName";

    @Adjacency(label = "javaClassFacet", direction = Direction.IN)
    public Iterable<ResourceModel> getResources();

    @Adjacency(label = "javaClassFacet", direction = Direction.IN)
    public void addResource(ResourceModel resource);

    @Property("className")
    public void setClassName(String className);

    @Property("className")
    public String getClassName();

    @Label
    @Property(PROPERTY_QUALIFIED_NAME)
    public String getQualifiedName();

    @Property(PROPERTY_QUALIFIED_NAME)
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
    public void addImport(final JavaClassModel javaImport);

    @Adjacency(label = "imports", direction = Direction.OUT)
    public Iterable<JavaClassModel> getImports();

    @Adjacency(label = "extends", direction = Direction.OUT)
    public JavaClassModel getExtends();

    @Adjacency(label = "extends", direction = Direction.OUT)
    public void setExtends(final JavaClassModel javaFacet);

    @Adjacency(label = "implements", direction = Direction.OUT)
    public void addImplements(final JavaClassModel javaFacet);

    @Adjacency(label = "implements", direction = Direction.OUT)
    public Iterable<JavaClassModel> getImplements();

    @GremlinGroovy("it.in('javaClassFacet').in('child').dedup")
    public Iterable<JarArchiveModel> providedBy();

    @Adjacency(label = "decompiledSource", direction = Direction.OUT)
    public void setDecompiledSource(FileModel source);

    @Adjacency(label = "decompiledSource", direction = Direction.OUT)
    public FileModel getDecompiledSource();

    @Adjacency(label = "originalSource", direction = Direction.OUT)
    public void setOriginalSource(FileModel source);

    @Adjacency(label = "originalSource", direction = Direction.OUT)
    public FileModel getOriginalSource();

    @GremlinGroovy("it.out('javaMethod').has('methodName', methodName)")
    public Iterable<JavaMethodModel> getMethod(@GremlinParam("methodName") String methodName);

    @Adjacency(label = "javaMethod", direction = Direction.OUT)
    public void addJavaMethod(final JavaMethodModel javaMethod);

    @Adjacency(label = "javaMethod", direction = Direction.OUT)
    public Iterable<JavaMethodModel> getJavaMethods();

    @GremlinGroovy("it.in('javaClassFacet').in('childArchiveEntry')")
    public Iterable<ArchiveModel> getArchivesProvidingClass();

    @GremlinGroovy("it.sideEffect{x=it}.out('extends', 'imports', 'implements').dedup().filter{it!=x}")
    public Iterable<JavaClassModel> dependsOnJavaClass();

    @GremlinGroovy("it.sideEffect{x=it}.in('extends', 'imports', 'implements').dedup().filter{it!=x}")
    public Iterable<JavaClassModel> providesForJavaClass();

    // TODO: Remove - residual of Brad's first prototype.
    @Property("blacklistCandidate")
    public void setBlacklistCandidate(boolean blacklistCandidate);

    @Property("blacklistCandidate")
    public boolean isBlacklistCandidate();

    // TODO: Rename to "isAnalyze" - see #windup discussion form June 11th.
    @Property("customerPackage")
    public void setCustomerPackage(boolean customerPackage);

    @Property("customerPackage")
    public boolean isCustomerPackage();

}
