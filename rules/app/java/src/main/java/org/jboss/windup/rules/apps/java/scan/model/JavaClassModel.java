package org.jboss.windup.rules.apps.java.scan.model;

import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.ResourceModel;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.annotations.gremlin.GremlinParam;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.reporting.renderer.api.Label;


// TODO:  WINDUP-85 - Introduce JavaFileModel
@TypeValue("JavaClassResource")
public interface JavaClassModel extends WindupVertexFrame
{
    public static final String PROPERTY_QUALIFIED_NAME = "qualifiedName";
    public static final String PROPERTY_CLASS_NAME = "className";
    public static final String PROPERTY_PACKAGE_NAME = "packageName";

    public static final String PROPERTY_CUSTOMER_PACKAGE = "customerPackage";
    public static final String PROPERTY_BLACKLIST_CANDIDATE = "blacklistCandidate";
    public static final String PROPERTY_MAJOR_VERSION = "majorVersion";
    public static final String PROPERTY_MINOR_VERSION = "minorVersion";

    
    @Adjacency(label = "javaClassFacet", direction = Direction.IN)
    public Iterable<ResourceModel> getResources();

    @Adjacency(label = "javaClassFacet", direction = Direction.IN)
    public void addResource(ResourceModel resource);

    
    // Properties derivable just from the file name.
    @Property(PROPERTY_CLASS_NAME)
    public void setClassName(String className);

    @Property(PROPERTY_CLASS_NAME)
    public String getClassName();

    @Label
    @Property(PROPERTY_QUALIFIED_NAME)
    public String getQualifiedName();

    @Property(PROPERTY_QUALIFIED_NAME)
    public void setQualifiedName(String qualifiedName);

    @Property(PROPERTY_PACKAGE_NAME)
    public String getPackageName();

    @Property(PROPERTY_PACKAGE_NAME)
    public void setPackageName(String packageName);

    
    
    @Property(PROPERTY_MAJOR_VERSION)
    public int getMajorVersion();

    @Property(PROPERTY_MAJOR_VERSION)
    public void setMajorVersion(int majorVersion);

    @Property(PROPERTY_MINOR_VERSION)
    public int getMinorVersion();

    @Property(PROPERTY_MINOR_VERSION)
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

    @Adjacency(label = "classFile", direction = Direction.OUT)
    public FileModel getClassFile();

    @Adjacency(label = "classFile", direction = Direction.OUT)
    public FileModel setClassFile(FileModel file);

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
    @Property(PROPERTY_BLACKLIST_CANDIDATE)
    public void setBlacklistCandidate(boolean blacklistCandidate);

    @Property(PROPERTY_BLACKLIST_CANDIDATE)
    public boolean isBlacklistCandidate();

    // TODO: Rename to "isAnalyze" - see #windup discussion form June 11th.
    @Property(PROPERTY_CUSTOMER_PACKAGE)
    public void setCustomerPackage(boolean customerPackage);

    @Property(PROPERTY_CUSTOMER_PACKAGE)
    public boolean isCustomerPackage();

}
