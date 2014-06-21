package org.jboss.windup.rules.apps.xml.dao;

import java.util.Iterator;
import org.jboss.windup.graph.dao.BaseDao;

import org.jboss.windup.graph.model.meta.xml.DoctypeMetaModel;

public interface DoctypeDao extends BaseDao<DoctypeMetaModel> {

	public Iterable<DoctypeMetaModel> findSystemIdOrPublicIdMatchingRegex(String ... regex);
	public Iterable<DoctypeMetaModel> findSystemIdMatchingRegex(String ... regex);
	public Iterable<DoctypeMetaModel> findPublicIdMatchingRegex(String ... regex);
	public Iterator<DoctypeMetaModel> findByProperties(String publicId, String systemId);

}
