package org.jboss.windup.graph.model.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.jboss.windup.engine.util.xml.LocationAwareXmlReader;
import org.w3c.dom.Document;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("ArchiveEntryResource")
public interface ArchiveEntryResource extends Resource {

	@Property("archiveEntry")
	public String getArchiveEntry();

	@Property("archiveEntry")
	public void setArchiveEntry(String archiveEntry);
	
	@Adjacency(label="child", direction=Direction.IN)
	public ArchiveResource getArchive();
	
	@Adjacency(label="child", direction=Direction.IN)
	public void setArchive(ArchiveResource archive);
	
	@JavaHandler
	public InputStream asInputStream();
	
	abstract class Impl implements ArchiveEntryResource, JavaHandlerContext<Vertex> {

	    public InputStream asInputStream() {
	        try {
    	        ZipFile zipFile = new ZipFile(new File(getArchive().getFileResource().getFilePath()));
    	        ZipEntry zipEntry = zipFile.getEntry(getArchiveEntry());
    	        InputStream is = zipFile.getInputStream(zipEntry);
    	        
    	        return is;
	        } catch (IOException e) {
	            throw new RuntimeException(e);
	        }
	    }
    }
}
