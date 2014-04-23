package org.jboss.windup.graph.model.resource;

import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipFile;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("ArchiveResourceFacet")
public interface ArchiveResource extends Resource {

	@Adjacency(label="archiveResourceFacet", direction=Direction.IN)
	public Resource getResource();

	@Adjacency(label="archiveResourceFacet", direction=Direction.IN)
	public void setResource(Resource resource);
	
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
	
	@Adjacency(label="childArchive", direction=Direction.OUT)
	public Iterable<ArchiveResource> getChildrenArchive();
	
	@Adjacency(label="childArchive", direction=Direction.OUT)
	public void addChildArchive(final ArchiveResource resource);
	
	@Adjacency(label="childArchive", direction=Direction.IN)
	public ArchiveResource getParentArchive();
	
	
	@Adjacency(label="childArchiveEntry", direction=Direction.OUT)
	public Iterable<ArchiveEntryResource> getChildrenArchiveEntries();
	
	@Adjacency(label="childArchiveEntry", direction=Direction.OUT)
	public void addChildrenArchiveEntries(final ArchiveEntryResource resource);

	@JavaHandler
	public File asFile() throws RuntimeException;

	@JavaHandler
	public ZipFile asZipFile() throws RuntimeException;
	
	@JavaHandler
	public InputStream asInputStream() throws RuntimeException;
	
	abstract class Impl implements ArchiveResource, JavaHandlerContext<Vertex> {

		@Override
		public InputStream asInputStream() throws RuntimeException {
			
			try {
				Resource underlyingResource = this.getResource();
				if(underlyingResource instanceof ArchiveEntryResource) {
						ArchiveEntryResource resource = frame(underlyingResource.asVertex(), ArchiveEntryResource.class);
						return resource.asInputStream();
				}
				else if(underlyingResource instanceof FileResource) {
					FileResource resource = frame(underlyingResource.asVertex(), FileResource.class);
					return resource.asInputStream();
				}
				
				return this.getResource().asInputStream();
			}
			catch(Exception e) {
				throw new RuntimeException("Exception reading resource.", e);
			}
		}
		
		@Override
		public File asFile() throws RuntimeException {
			try {
				Resource underlyingResource = this.getResource();
				if(underlyingResource instanceof ArchiveEntryResource) {
					ArchiveEntryResource resource = frame(underlyingResource.asVertex(), ArchiveEntryResource.class);
					return resource.asFile();
				}
				else if(underlyingResource instanceof FileResource) {
					FileResource resource = frame(underlyingResource.asVertex(), FileResource.class);
					return resource.asFile();
				}
				return this.getResource().asFile();
			}
			catch(Exception e) {
				throw new RuntimeException("Exception reading resource.", e);
			}
		}
		
		@Override
		public ZipFile asZipFile() throws RuntimeException {
			try {
				return new ZipFile(this.asFile());
			}
			catch(Exception e) {
				throw new RuntimeException("Exception reading resource.", e);
			}
		}
	}
}
