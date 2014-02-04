package org.jboss.windup.graph.dao;

import java.util.Iterator;

import org.jboss.windup.graph.model.meta.xml.DoctypeMeta;

public interface DoctypeDao extends BaseDao<DoctypeMeta> {

	public Iterator<DoctypeMeta> findByProperties(String name, String publicId, String systemId, String baseURI);

}
