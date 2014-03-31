package org.jboss.windup.graph.dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jboss.windup.graph.model.resource.FileResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkaurelius.titan.core.attribute.Text;
import com.thinkaurelius.titan.util.datastructures.IterablesUtil;

public class FileResourceDao extends BaseDao<FileResource> {

	private static final Logger LOG = LoggerFactory.getLogger(FileResourceDao.class);
	
	public FileResourceDao() {
		super(FileResource.class);
	}

	public FileResource getByFilePath(String filePath) {
		FileResource entry = getByUniqueProperty("filePath", filePath);
		
		if(entry == null) {
			entry = this.create(null);
			entry.setFilePath(filePath);
			context.getGraph().commit();
		}
		
		return entry;
	}
	
	public InputStream getPayload(FileResource file) throws IOException {
		String fileName = file.getFilePath();
		FileInputStream fis = new FileInputStream(new java.io.File(fileName));
		return fis;
	}
	
	public Iterable<FileResource> findArchiveEntryWithExtension(String ... values) {
		//build regex
		if(values.length == 0) {
			return IterablesUtil.emptyIterable();
		}
		
		final String regex;
		if(values.length == 1) {
			regex = ".+\\."+values[0]+"$";
		}
		else {
			StringBuilder builder = new StringBuilder();
			builder.append("\\b(");
			int i=0;
			for(String value : values) {
				builder.append("|");
				builder.append(value);
				i++;
			}
			builder.append(")\\b");
			regex = ".+\\."+builder.toString()+"$";
		}

		LOG.debug("Regex: "+regex);
		return context.getFramed().query().has("type", typeValue).has("filePath", Text.REGEX, regex).vertices(type);
	}
}
