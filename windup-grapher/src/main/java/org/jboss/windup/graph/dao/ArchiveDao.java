package org.jboss.windup.graph.dao;

import java.util.Collection;

import org.jboss.windup.graph.dao.exception.ArchiveIndexReaderException;
import org.jboss.windup.graph.dao.exception.ArchiveIndexWriteException;
import org.jboss.windup.graph.model.resource.JarArchive;

public interface ArchiveDao {
	
	public void addArchive(JarArchive archive) throws ArchiveIndexWriteException;
	public Collection<JarArchive> findArchiveByMD5(String value) throws ArchiveIndexReaderException;
	public Collection<JarArchive> findArchiveBySHA1(String value) throws ArchiveIndexReaderException;
	public Collection<JarArchive> findArchiveByName(String value) throws ArchiveIndexReaderException;
	public Collection<JarArchive> findArchiveByNameAndVersion(String value, String version) throws ArchiveIndexReaderException;
	public Collection<JarArchive> findArchiveByField(String field, String value) throws ArchiveIndexReaderException;
	public Collection<JarArchive> findArchiveByQualifiedClassName(String clz) throws ArchiveIndexReaderException;
	public Collection<JarArchive> findArchiveLeveragingDependency(String clz) throws ArchiveIndexReaderException;
	
}
