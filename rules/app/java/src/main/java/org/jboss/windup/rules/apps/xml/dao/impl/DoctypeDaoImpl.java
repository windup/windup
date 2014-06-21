package org.jboss.windup.rules.apps.xml.dao.impl;

import java.util.Iterator;

import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.rules.apps.xml.dao.DoctypeDao;
import org.jboss.windup.graph.model.meta.xml.DoctypeMetaModel;

import com.google.common.collect.Iterables;
import com.tinkerpop.frames.FramedGraphQuery;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import org.jboss.windup.graph.dao.impl.BaseDaoImpl;

@Singleton
public class DoctypeDaoImpl extends BaseDaoImpl<DoctypeMetaModel> implements DoctypeDao {

	public DoctypeDaoImpl() {
		super(DoctypeMetaModel.class);
	}

	public Iterable<DoctypeMetaModel> findSystemIdOrPublicIdMatchingRegex(String ... regex) {
		//iterate through all vertices
 		Iterable<DoctypeMetaModel> results = Iterables.concat(findSystemIdMatchingRegex(regex), findPublicIdMatchingRegex(regex));
		GremlinPipeline<DoctypeMetaModel, DoctypeMetaModel> pipe = new GremlinPipeline<DoctypeMetaModel, DoctypeMetaModel>(results).dedup();
		return pipe;
	}
	
	
	public Iterable<DoctypeMetaModel> findSystemIdMatchingRegex(String ... regex) {
		return super.findValueMatchingRegex("systemId", regex);
	}
	
	public Iterable<DoctypeMetaModel> findPublicIdMatchingRegex(String ... regex) {
		return super.findValueMatchingRegex("publicId", regex);
	}
	

	public Iterator<DoctypeMetaModel> findByProperties(String publicId, String systemId) {
		FramedGraphQuery query = getContext().getFramed().query();
		if(StringUtils.isNotBlank(publicId)) {
			query.has("publicId", publicId);
		}
		if(StringUtils.isNotBlank(systemId)) {
			query.has("systemId", systemId);
		}
		return query.vertices(DoctypeMetaModel.class).iterator();
	}

}
