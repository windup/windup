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
public interface ArchiveResourceModel extends ResourceModel {

	@Adjacency(label="archiveResourceFacet", direction=Direction.IN)
	public ResourceModel getParentResource();

	@Adjacency(label="archiveResourceFacet", direction=Direction.IN)
	public void setParentResource(ResourceModel resource);
	
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
	public Iterable<ArchiveResourceModel> getChildrenArchive();
	
	@Adjacency(label="childArchive", direction=Direction.OUT)
	public void addChildArchive(final ArchiveResourceModel resource);
	
	@Adjacency(label="childArchive", direction=Direction.IN)
	public ArchiveResourceModel getParentArchive();
	
	
	@Adjacency(label="childArchiveEntry", direction=Direction.OUT)
	public Iterable<ArchiveEntryResourceModel> getChildrenArchiveEntries();
	
	@Adjacency(label="childArchiveEntry", direction=Direction.OUT)
	public void addChildrenArchiveEntries(final ArchiveEntryResourceModel resource);

	@JavaHandler
	public File asFile() throws RuntimeException;

	@JavaHandler
	public ZipFile asZipFile() throws RuntimeException;
	
	@JavaHandler
	public InputStream asInputStream() throws RuntimeException;
	
	abstract class Impl implements ArchiveResourceModel, JavaHandlerContext<Vertex> {

		@Override
		public InputStream asInputStream() throws RuntimeException {
			
			try {
				ResourceModel underlyingResource = this.getParentResource();
				if(underlyingResource instanceof ArchiveEntryResourceModel) {
						ArchiveEntryResourceModel resource = frame(underlyingResource.asVertex(), ArchiveEntryResourceModel.class);
						return resource.asInputStream();
				}
				else if(underlyingResource instanceof FileResourceModel) {
					FileResourceModel resource = frame(underlyingResource.asVertex(), FileResourceModel.class);
					return resource.asInputStream();
				}
				
				return this.getParentResource().asInputStream();
			}
			catch(Exception e) {
				throw new RuntimeException("Exception reading resource.", e);
			}
		}
		
		@Override
		public File asFile() throws RuntimeException {
			try {
				ResourceModel underlyingResource = this.getParentResource();
				if(underlyingResource instanceof ArchiveEntryResourceModel) {
					ArchiveEntryResourceModel resource = frame(underlyingResource.asVertex(), ArchiveEntryResourceModel.class);
					return resource.asFile();
				}
				else if(underlyingResource instanceof FileResourceModel) {
					FileResourceModel resource = frame(underlyingResource.asVertex(), FileResourceModel.class);
					return resource.asFile();
				}
				return this.getParentResource().asFile();
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
