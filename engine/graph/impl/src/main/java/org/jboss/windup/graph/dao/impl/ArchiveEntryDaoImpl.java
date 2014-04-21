package org.jboss.windup.graph.dao.impl;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.ArchiveEntryDao;
import org.jboss.windup.graph.model.resource.ArchiveEntryResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thinkaurelius.titan.core.attribute.Text;
import com.thinkaurelius.titan.util.datastructures.IterablesUtil;
import com.tinkerpop.gremlin.java.GremlinPipeline;

@Singleton
public class ArchiveEntryDaoImpl extends BaseDaoImpl<ArchiveEntryResource> implements ArchiveEntryDao {

	private static Logger LOG = LoggerFactory.getLogger(ArchiveEntryDaoImpl.class);
	
	public ArchiveEntryDaoImpl() {
		super(ArchiveEntryResource.class);
	}
	
	public Iterable<ArchiveEntryResource> findArchiveEntry(String value) {
		return super.getByProperty("archiveEntry", value);
	}

	public long findArchiveEntryWithExtensionCount(String ... values) {
		GremlinPipeline pipe = new GremlinPipeline();
		return pipe.start(findArchiveEntryWithExtension(values)).count();
	}
	
	public Iterable<ArchiveEntryResource> findArchiveEntryWithExtension(String ... values) {
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
				if(i>0) {
					builder.append("|");
				}
				builder.append(value);
				i++;
			}
			builder.append(")\\b");
			regex = ".+\\."+builder.toString()+"$";
		}

		LOG.debug("Regex: "+regex);
		return context.getFramed().query().has("type", typeValue).has("archiveEntry", Text.REGEX, regex).vertices(type);
	}
}
