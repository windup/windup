package org.jboss.windup.graph.model.resource;

import java.util.Iterator;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("ArchiveResource")
public interface Archive extends File {
	

	@Property("md5Hash")
	public String getMD5Hash();
	
	@Property("md5Hash")
	public void setMD5Hash(String md5Hash);
	
	@Property("sha1Hash")
	public String getSHA1Hash();
	
	@Property("sha1Hash")
	public void setSHA1Hash(String sha1Hash);
	
	
	@Property("archiveName")
	public String getArchiveName();
	
	@Property("archiveName")
	public void setArchiveName(String archiveName);
	
	@Adjacency(label="child", direction=Direction.OUT)
	public Iterator<Resource> getChildren();
	
	@Adjacency(label="child", direction=Direction.OUT)
	public void addChild(final Resource resource);
	
	@Adjacency(label="child", direction=Direction.IN)
	public Resource getParent();
	
	@Adjacency(label="child", direction=Direction.IN)
	public void setChild(final Resource resource);
	
}
