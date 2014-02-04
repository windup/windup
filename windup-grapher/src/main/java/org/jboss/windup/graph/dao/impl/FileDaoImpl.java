package org.jboss.windup.graph.dao.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.dao.FileDao;
import org.jboss.windup.graph.model.resource.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkaurelius.titan.core.attribute.Text;
import com.thinkaurelius.titan.util.datastructures.IterablesUtil;

public class FileDaoImpl extends BaseDaoImpl<File> implements FileDao {

	private static final Logger LOG = LoggerFactory.getLogger(FileDaoImpl.class);
	
	public FileDaoImpl(GraphContext context) {
		super(context, File.class);
	}

	@Override
	public File getByFilePath(String filePath) {
		File entry = getByUniqueProperty("filePath", filePath);
		
		if(entry == null) {
			entry = this.create(null);
			entry.setFilePath(filePath);
			context.getGraph().commit();
		}
		
		return entry;
	}
	
	public InputStream getPayload(File file) throws IOException {
		String fileName = file.getFilePath();
		FileInputStream fis = new FileInputStream(new java.io.File(fileName));
		return fis;
	}
	
	@Override
	public Iterable<File> findArchiveEntryWithExtension(String ... values) {
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

		LOG.info("Regex: "+regex);
		return context.getFramed().query().has("type", typeValue).has("filePath", Text.REGEX, regex).vertices(type);
	}
}
