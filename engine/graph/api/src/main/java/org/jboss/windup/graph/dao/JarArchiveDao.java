package org.jboss.windup.graph.dao;

import org.jboss.windup.graph.dao.exception.ArchiveIndexReaderException;
import org.jboss.windup.graph.model.resource.JarArchive;

public interface JarArchiveDao extends BaseDao<JarArchive> {

	public Iterable<JarArchive> findArchiveByMD5(String value) throws ArchiveIndexReaderException;

	public Iterable<JarArchive> findArchiveBySHA1(String value) throws ArchiveIndexReaderException;

	public Iterable<JarArchive> findArchiveByName(String value) throws ArchiveIndexReaderException;
	
	public Iterable<JarArchive> findArchiveByQualifiedClassName(String clz) throws ArchiveIndexReaderException;

	public Iterable<JarArchive> findUnusedJars();

	public Iterable<JarArchive> findCircularReferences(JarArchive archive);
	
	
}
