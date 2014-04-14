package org.jboss.windup.graph.model.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("ArchiveResource")
public interface ArchiveResource extends Resource {

	@Adjacency(label="file", direction=Direction.OUT)
	public FileResource getFileResource();

	@Adjacency(label="file", direction=Direction.OUT)
	public void setFileResource(FileResource file);

	
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
	public Iterator<ArchiveResource> getChildren();
	
	@Adjacency(label="child", direction=Direction.OUT)
	public void addChild(final ArchiveResource resource);
	
	@Adjacency(label="child", direction=Direction.IN)
	public ArchiveResource getParent();
	
	@Adjacency(label="child", direction=Direction.IN)
	public void setChild(final ArchiveResource resource);
	
	@JavaHandler
    public InputStream asInputStream() throws FileNotFoundException;
	
	@JavaHandler
    public File asFile() throws FileNotFoundException;
    
    abstract class Impl implements ArchiveResource, JavaHandlerContext<Vertex> {
        public File asFile() {
            return new File(getFileResource().getFilePath());
        }
        
        @Override
        public InputStream asInputStream() throws FileNotFoundException {
            if(getFileResource().getFilePath() != null) {
                File file = new File(getFileResource().getFilePath());
                return new FileInputStream(file);
            }
            return null;
        }
    }
}
