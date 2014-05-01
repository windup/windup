package org.jboss.windup.graph.model.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("FileResource")
public interface FileResourceModel extends ResourceModel {
	
	@Property("filePath")
	public String getFilePath();
	
	@Property("filePath")
	public void setFilePath(String filePath);
	
	@JavaHandler
	public File asFile() throws RuntimeException;
	
	@JavaHandler
	public InputStream asInputStream() throws RuntimeException;

	abstract class Impl implements FileResourceModel, ResourceModel, JavaHandlerContext<Vertex> {
		
		@Override
		public InputStream asInputStream() throws RuntimeException {
			try {
				if(this.getFilePath() != null) {
					File file = new File(getFilePath());
					return new FileInputStream(file);
				}
				return null;
			}
			catch(Exception e) {
				throw new RuntimeException("Exception reading resource.", e);
			}
		}
		
		@Override
		public File asFile() throws RuntimeException {
			if(this.getFilePath() != null) {
				File file = new File(getFilePath());
				return file;
			}
			return null; 
		}
	}
}
