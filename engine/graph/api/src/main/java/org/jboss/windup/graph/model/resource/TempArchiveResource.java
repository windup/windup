package org.jboss.windup.graph.model.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Indicates an archive that was exploded from another archive.
 * 
 * @author bradsdavis@gmail.com
 *
 */
@TypeValue("TempArchiveResource")
public interface TempArchiveResource extends FileResource {
	
	@Property("archiveEntry")
	public String getArchiveEntry();

	@Property("archiveEntry")
	public void setArchiveEntry(String archiveEntry);
	
	@Adjacency(label="child", direction=Direction.IN)
	public ArchiveResource getArchive();
	
	@Adjacency(label="child", direction=Direction.IN)
	public void setArchive(ArchiveResource archive);
	
	@JavaHandler
	public File asFile();
	@JavaHandler
    public InputStream asInputStream();

    abstract class Impl implements TempArchiveResource, JavaHandlerContext<Vertex> {
    	public File asFile() {
            return new File(getFilePath());
        }
    
        public InputStream asInputStream() {
            try {
                FileInputStream fis = new FileInputStream(asFile());
                return fis;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
