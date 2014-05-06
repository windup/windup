package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.FileResourceDao;
import org.jboss.windup.graph.model.resource.FileResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkaurelius.titan.core.attribute.Text;
import com.thinkaurelius.titan.util.datastructures.IterablesUtil;

@Singleton
public class FileResourceDaoImpl extends BaseDaoImpl<FileResourceModel> implements FileResourceDao {

	private static final Logger LOG = LoggerFactory.getLogger(FileResourceDaoImpl.class);
	
	public FileResourceDaoImpl() {
		super(FileResourceModel.class);
	}

	public FileResourceModel createByFilePath(String filePath) {
		FileResourceModel entry = getByUniqueProperty("filePath", filePath);
		
		if(entry == null) {
			entry = this.create();
			entry.setFilePath(filePath);
			context.getGraph().commit();
		}
		
		return entry;
	}
	
	public Iterable<FileResourceModel> findArchiveEntryWithExtension(String ... values) {
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
			for(String value : values) {
				builder.append("|");
				builder.append(value);
			}
			builder.append(")\\b");
			regex = ".+\\."+builder.toString()+"$";
		}

		LOG.debug("Regex: "+regex);
		return context.getFramed().query().has("type", Text.CONTAINS, typeValueForSearch).has("filePath", Text.REGEX, regex).vertices(type);
	}
}
