package org.jboss.windup.classprofiler;

import java.util.Collection;

import org.jboss.windup.classprofiler.exception.ClassIndexReaderException;
import org.jboss.windup.classprofiler.exception.ClassIndexWriteException;
import org.jboss.windup.classprofiler.metadata.ArchiveVO;
import org.jboss.windup.classprofiler.metadata.ClassVO;

public interface ClassIndexer {

	public void addClass(ArchiveVO archive, ClassVO clz) throws ClassIndexWriteException;
	public Collection<String> findClassDependenciesByQualifiedName(String clz) throws ClassIndexReaderException;
	public Collection<ClassVO> findClassesLeveragingDependency(String clz) throws ClassIndexReaderException;
	
}
