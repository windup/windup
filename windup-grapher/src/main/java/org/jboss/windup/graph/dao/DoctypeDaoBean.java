package org.jboss.windup.graph.dao;

import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.graph.model.meta.xml.DoctypeMeta;

import com.tinkerpop.frames.FramedGraphQuery;

public class DoctypeDaoBean extends BaseDaoBean<DoctypeMeta> {

	public DoctypeDaoBean() {
		super(DoctypeMeta.class);
	}

	public Iterator<DoctypeMeta> findByProperties(String name, String publicId, String systemId, String baseURI) {
		FramedGraphQuery query = context.getFramed().query();
		if(StringUtils.isNotBlank(name)) {
			query.has("name", name);
		}
		if(StringUtils.isNotBlank(publicId)) {
			query.has("publicId", publicId);
		}
		if(StringUtils.isNotBlank(systemId)) {
			query.has("systemId", systemId);
		}
		if(StringUtils.isNotBlank(baseURI)) {
			query.has("baseURI", baseURI);
		}
		
		return query.vertices(DoctypeMeta.class).iterator();
	}

}
