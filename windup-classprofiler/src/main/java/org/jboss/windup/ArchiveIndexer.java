package org.jboss.windup;

import java.util.Collection;

import org.jboss.windup.exception.ArchiveIndexReaderException;
import org.jboss.windup.exception.ArchiveIndexWriteException;
import org.jboss.windup.metadata.ArchiveVO;

public interface ArchiveIndexer {
	public void addArchive(ArchiveVO archive) throws ArchiveIndexWriteException;
	public Collection<String> findArchiveByField(String field, String value) throws ArchiveIndexReaderException;
	public Collection<String> findArchiveByQualifiedClassName(String clz) throws ArchiveIndexReaderException;
	public Collection<String> findArchiveLeveragingDependency(String clz) throws ArchiveIndexReaderException;
	
}
