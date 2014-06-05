package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.dao.exception.ArchiveIndexReaderException;
import org.jboss.windup.graph.model.resource.JarArchiveModel;

public interface JarArchiveDao extends BaseDao<JarArchiveModel> {

	public Iterable<JarArchiveModel> findArchiveByMD5(String value) throws ArchiveIndexReaderException;

	public Iterable<JarArchiveModel> findArchiveBySHA1(String value) throws ArchiveIndexReaderException;

	public Iterable<JarArchiveModel> findArchiveByName(String value) throws ArchiveIndexReaderException;
	
	public Iterable<JarArchiveModel> findArchiveByQualifiedClassName(String clz) throws ArchiveIndexReaderException;

	public Iterable<JarArchiveModel> findUnusedJars();

	public Iterable<JarArchiveModel> findCircularReferences(JarArchiveModel archive);
	
	
}
