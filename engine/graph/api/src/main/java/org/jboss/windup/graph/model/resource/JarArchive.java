package org.jboss.windup.graph.model.resource;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import org.w3c.dom.Document;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("JarArchiveResource")
public interface JarArchive extends ArchiveResource {

	@GremlinGroovy("it.out('child').out('javaClassFacet')")
	public Iterable<JavaClass> getJavaClasses();
	
	@GremlinGroovy("it.sideEffect{x=it}.out('child').out('javaClassFacet').out('extends', 'imports', 'implements').in('javaClassFacet').in('child').dedup.filter{it!=x}")
	public Iterable<JarArchive> dependsOnArchives();

	@GremlinGroovy("it.sideEffect{x=it}.out('child').out('javaClassFacet').in('extends', 'imports', 'implements').dedup.in('javaClassFacet').in('child').dedup.filter{it!=x}")
	public Iterable<JarArchive> providesForArchives();
	
	@GremlinGroovy("it.out('child').out('xmlResourceFacet')")
	public Iterable<XmlResource> getXmlFiles();
	
	@JavaHandler
	public JarFile asJarFile();
	
	abstract class Impl implements JarArchive, JavaHandlerContext<Vertex> {

	    public JarFile asJarFile() {
	        File file = new File(getFileResource().getFilePath());
	        try {
	            return new JarFile(file);
	        } catch (IOException e) {
	            throw new RuntimeException(e);
	        }
	    }
	}
	
}
