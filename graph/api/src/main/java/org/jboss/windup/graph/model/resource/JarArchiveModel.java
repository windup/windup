package org.jboss.windup.graph.model.resource;

import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.JarManifestModel;
import org.jboss.windup.rules.apps.java.scan.model.JavaClassModel;

import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("JarArchiveResource")
public interface JarArchiveModel extends ArchiveModel
{

    @GremlinGroovy("it.out('childArchiveEntry').out('javaClassFacet')")
    public Iterable<JavaClassModel> getJavaClasses();

    @GremlinGroovy("it.sideEffect{x=it}.out('childArchiveEntry').out('javaClassFacet').out('extends', 'imports', 'implements').in('javaClassFacet').in('child').dedup.filter{it!=x}")
    public Iterable<JarArchiveModel> dependsOnArchives();

    @GremlinGroovy("it.sideEffect{x=it}.out('childArchiveEntry').out('javaClassFacet').in('extends', 'imports', 'implements').dedup.in('javaClassFacet').in('child').dedup.filter{it!=x}")
    public Iterable<JarArchiveModel> providesForArchives();

    @GremlinGroovy("it.out('childArchiveEntry').out('xmlResourceFacet')")
    public Iterable<XmlResourceModel> getXmlFiles();

    @GremlinGroovy("it.out('childArchiveEntry').out('manifestFacet')")
    public JarManifestModel getJarManifest();
}
