package org.jboss.windup.graph.dao;

import java.util.Iterator;

import org.jboss.windup.graph.dao.exception.ArchiveIndexReaderException;
import org.jboss.windup.graph.dao.exception.ArchiveIndexWriteException;
import org.jboss.windup.graph.model.resource.facet.JarArchiveFacet;

public interface JarArchiveDao extends BaseDao<JarArchiveFacet> {
	
	public void addArchive(JarArchiveFacet archive) throws ArchiveIndexWriteException;
	public Iterator<JarArchiveFacet> findArchiveByMD5(String value) throws ArchiveIndexReaderException;
	public Iterator<JarArchiveFacet> findArchiveBySHA1(String value) throws ArchiveIndexReaderException;
	public Iterator<JarArchiveFacet> findArchiveByName(String value) throws ArchiveIndexReaderException;
	public Iterator<JarArchiveFacet> findArchiveByNameAndVersion(String value, String version) throws ArchiveIndexReaderException;
	public Iterator<JarArchiveFacet> findArchiveByField(String field, String value) throws ArchiveIndexReaderException;
	public Iterator<JarArchiveFacet> findArchiveByQualifiedClassName(String clz) throws ArchiveIndexReaderException;
	public Iterator<JarArchiveFacet> findArchiveLeveragingDependency(String clz) throws ArchiveIndexReaderException;
	
}
