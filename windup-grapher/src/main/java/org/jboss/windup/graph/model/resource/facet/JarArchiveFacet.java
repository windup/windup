package org.jboss.windup.graph.model.resource.facet;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("JarArchiveFacet")
public interface JarArchiveFacet extends ResourceFacet {

	@Property("md5Hash")
	public String getMD5Hash();
	
	@Property("md5Hash")
	public void setMD5Hash(String md5Hash);
	
	@Property("sha1Hash")
	public String getSHA1Hash();
	
	@Property("sha1Hash")
	public void setSHA1Hash(String sha1Hash);
}
