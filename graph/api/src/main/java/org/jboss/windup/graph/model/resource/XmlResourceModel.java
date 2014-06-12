package org.jboss.windup.graph.model.resource;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.jboss.windup.util.xml.LocationAwareXmlReader;
import org.jboss.windup.graph.model.meta.xml.DoctypeMetaModel;
import org.jboss.windup.graph.model.meta.xml.NamespaceMetaModel;
import org.w3c.dom.Document;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("XmlResource")
public interface XmlResourceModel extends ResourceModel {

	@Adjacency(label="xmlResourceFacet", direction=Direction.IN)
	public ResourceModel getResource();

	@Adjacency(label="xmlResourceFacet", direction=Direction.IN)
	public void setResource(ResourceModel resource);

	@Adjacency(label="doctype", direction=Direction.OUT)
	public void setDoctype(DoctypeMetaModel doctype);
	
	@Adjacency(label="doctype", direction=Direction.OUT)
	public DoctypeMetaModel getDoctype();

	@Adjacency(label="namespace", direction=Direction.OUT)
	public void addNamespace(NamespaceMetaModel namespace);
	
	@Adjacency(label="namespace", direction=Direction.OUT)
	public Iterable<NamespaceMetaModel> getNamespaces();
	
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
	
	
	abstract class Impl implements XmlResourceModel, JavaHandlerContext<Vertex> {
		
		@Override
		public InputStream asInputStream() throws RuntimeException {
			try {
				ResourceModel underlyingResource = this.getResource();
				if(underlyingResource instanceof ArchiveEntryResourceModel) {
						ArchiveEntryResourceModel resource = frame(underlyingResource.asVertex(), ArchiveEntryResourceModel.class);
						return resource.asInputStream();
				}
				else if(underlyingResource instanceof FileModel) {
					FileModel resource = frame(underlyingResource.asVertex(), FileModel.class);
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
				ResourceModel underlyingResource = this.getResource();
				if(underlyingResource instanceof ArchiveEntryResourceModel) {
					ArchiveEntryResourceModel resource = frame(underlyingResource.asVertex(), ArchiveEntryResourceModel.class);
					return resource.asFile();
				}
				else if(underlyingResource instanceof FileModel) {
					FileModel resource = frame(underlyingResource.asVertex(), FileModel.class);
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
			ResourceModel underlyingResource = this.getResource();
			if(underlyingResource instanceof ArchiveEntryResourceModel) {
				InputStream is = null;
				try {
					ArchiveEntryResourceModel resource = frame(underlyingResource.asVertex(), ArchiveEntryResourceModel.class);
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
