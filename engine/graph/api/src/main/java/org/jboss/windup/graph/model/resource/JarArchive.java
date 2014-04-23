package org.jboss.windup.graph.model.resource;

import org.jboss.windup.graph.model.meta.JarManifest;

import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("JarArchiveResource")
public interface JarArchive extends ArchiveResource {

    @GremlinGroovy("it.out('childArchiveEntry').out('javaClassFacet')")
    public Iterable<JavaClass> getJavaClasses();
    
    @GremlinGroovy("it.sideEffect{x=it}.out('childArchiveEntry').out('javaClassFacet').out('extends', 'imports', 'implements').in('javaClassFacet').in('child').dedup.filter{it!=x}")
    public Iterable<JarArchive> dependsOnArchives();

    @GremlinGroovy("it.sideEffect{x=it}.out('childArchiveEntry').out('javaClassFacet').in('extends', 'imports', 'implements').dedup.in('javaClassFacet').in('child').dedup.filter{it!=x}")
    public Iterable<JarArchive> providesForArchives();
    
    @GremlinGroovy("it.out('childArchiveEntry').out('xmlResourceFacet')")
    public Iterable<XmlResource> getXmlFiles();

    @GremlinGroovy("it.out('childArchiveEntry').out('manifestFacet')")
    public JarManifest getJarManifest();
}
