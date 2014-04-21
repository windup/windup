package org.jboss.windup.graph.dao;

import java.util.Iterator;

import org.jboss.windup.graph.model.meta.xml.DoctypeMeta;

public interface DoctypeDao extends BaseDao<DoctypeMeta> {

	public Iterable<DoctypeMeta> findSystemIdOrPublicIdMatchingRegex(String ... regex);
	public Iterable<DoctypeMeta> findSystemIdMatchingRegex(String ... regex);
	public Iterable<DoctypeMeta> findPublicIdMatchingRegex(String ... regex);
	public Iterator<DoctypeMeta> findByProperties(String publicId, String systemId);

}
