package org.jboss.windup.graph.model.resource;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.jboss.windup.engine.util.xml.LocationAwareXmlReader;
import org.jboss.windup.graph.model.meta.xml.DoctypeMeta;
import org.jboss.windup.graph.model.meta.xml.NamespaceMeta;
import org.w3c.dom.Document;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("XmlResource")
public interface XmlResource extends Resource {

	@Adjacency(label="xmlResourceFacet", direction=Direction.IN)
	public Resource getResource();

	@Adjacency(label="xmlResourceFacet", direction=Direction.IN)
	public void setResource(Resource resource);

	@Adjacency(label="doctype", direction=Direction.OUT)
	public void setDoctype(DoctypeMeta doctype);
	
	@Adjacency(label="doctype", direction=Direction.OUT)
	public DoctypeMeta getDoctype();

	@Adjacency(label="namespace", direction=Direction.OUT)
	public void addNamespace(NamespaceMeta namespace);
	
	@Adjacency(label="namespace", direction=Direction.OUT)
	public Iterable<NamespaceMeta> getNamespaces();
	
	@Property("rootTagName")
	public String getRootTagName();

	@Property("rootTagName")
	public void setRootTagName(String rootTagName);
	
	@JavaHandler
	public Document asDocument();
	
	@JavaHandler
	public InputStream asInputStream() throws RuntimeException;
	
	@JavaHandler
	public File asFile() throws RuntimeException;
	
	
	abstract class Impl implements XmlResource, JavaHandlerContext<Vertex> {
		
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
		public Document asDocument() {
			Resource underlyingResource = this.getResource();
			if(underlyingResource instanceof ArchiveEntryResource) {
				InputStream is = null;
				try {
					ArchiveEntryResource resource = frame(underlyingResource.asVertex(), ArchiveEntryResource.class);
					is = resource.asInputStream();
					Document parsedDocument = LocationAwareXmlReader.readXML(is);
					return parsedDocument;
				}
				catch(Exception e) {
					throw new RuntimeException("Exception reading document.", e);
				}
				finally {
					IOUtils.closeQuietly(is);
				}
			}
			return null;
		}
		
	}
}
