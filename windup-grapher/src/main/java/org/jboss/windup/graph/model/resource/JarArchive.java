package org.jboss.windup.graph.model.resource;

import java.util.Iterator;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("JarArchiveResource")
public interface JarArchive extends Archive {

	@Property("md5Hash")
	public String getMD5Hash();
	
	@Property("md5Hash")
	public void setMD5Hash(String md5Hash);
	
	@Property("sha1Hash")
	public String getSHA1Hash();
	
	@Property("sha1Hash")
	public void setSHA1Hash(String sha1Hash);
	
	@GremlinGroovy("it.as('x').out('child').filter{'type','JavaClassResource'}.select")
	public Iterator<JavaClass> getJavaClasses();
	
	@GremlinGroovy("it.as('x').out('child').filter{'type','XmlResource'}.select")
	public Iterator<XmlResource> getXmlFiles();
	
	
}
