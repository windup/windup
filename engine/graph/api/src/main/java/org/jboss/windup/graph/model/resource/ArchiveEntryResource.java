package org.jboss.windup.graph.model.resource;

import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jboss.windup.engine.util.ZipUtil;

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
	
	@Adjacency(label="childArchiveEntry", direction=Direction.IN)
	public ArchiveResource getArchive();
	
	@Adjacency(label="childArchiveEntry", direction=Direction.IN)
	public void setArchive(ArchiveResource archive);

	@JavaHandler
	public InputStream asInputStream() throws RuntimeException;
	
	@JavaHandler
	public File asFile() throws RuntimeException;
	
	@JavaHandler
	public ZipEntry asZipEntry() throws RuntimeException;
	
	abstract class Impl implements ArchiveEntryResource, JavaHandlerContext<Vertex> {
		@Override
		public InputStream asInputStream() throws RuntimeException {
			try {
				ZipFile file = getArchive().asZipFile();
				ZipEntry entry = file.getEntry(this.getArchiveEntry());
				return file.getInputStream(entry);
			}
			catch(Exception e) {
				throw new RuntimeException("Exception reading resource.", e);
			}
		}
		
		@Override
		public File asFile() throws RuntimeException {
			try {
				if(this.it().getProperty("tempFile") != null) {
					File filePath = new File(this.it().getProperty("tempFile").toString());
					return filePath;
				}
				else {
					File temp = ZipUtil.unzipToTemp(this.getArchive().asZipFile(), this.asZipEntry());
					this.it().setProperty("tempFile", temp.getAbsolutePath());
					return temp;
				}
			}
			catch(Exception e) {
				throw new RuntimeException("Exception reading resource.", e);
			}
		}
		
		@Override
		public ZipEntry asZipEntry() throws RuntimeException {
			ZipEntry ze = new ZipEntry(this.getArchiveEntry());
			return ze;
		}
	}
}
