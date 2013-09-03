package org.jboss.windup.classprofiler;

import java.util.Collection;

import org.jboss.windup.classprofiler.exception.ArchiveIndexReaderException;
import org.jboss.windup.classprofiler.exception.ArchiveIndexWriteException;
import org.jboss.windup.classprofiler.metadata.ArchiveVO;

public interface ArchiveIndexer {
	
	public void addArchive(ArchiveVO archive) throws ArchiveIndexWriteException;
	public Collection<ArchiveVO> findArchiveByMD5(String value) throws ArchiveIndexReaderException;
	public Collection<ArchiveVO> findArchiveBySHA1(String value) throws ArchiveIndexReaderException;
	public Collection<ArchiveVO> findArchiveByName(String value) throws ArchiveIndexReaderException;
	public Collection<ArchiveVO> findArchiveByNameAndVersion(String value, String version) throws ArchiveIndexReaderException;
	public Collection<ArchiveVO> findArchiveByField(String field, String value) throws ArchiveIndexReaderException;
	public Collection<ArchiveVO> findArchiveByQualifiedClassName(String clz) throws ArchiveIndexReaderException;
	public Collection<ArchiveVO> findArchiveLeveragingDependency(String clz) throws ArchiveIndexReaderException;
	
}
