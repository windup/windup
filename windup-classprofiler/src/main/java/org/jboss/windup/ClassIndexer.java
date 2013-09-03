package org.jboss.windup;

import java.util.Collection;

import org.jboss.windup.exception.ClassIndexReaderException;
import org.jboss.windup.exception.ClassIndexWriteException;
import org.jboss.windup.metadata.ArchiveVO;
import org.jboss.windup.metadata.ClassVO;

public interface ClassIndexer {

	public void addClass(ArchiveVO archive, ClassVO clz) throws ClassIndexWriteException;
	public Collection<String> findClassDependenciesByQualifiedName(String clz) throws ClassIndexReaderException;
	public Collection<ClassVO> findClassesLeveragingDependency(String clz) throws ClassIndexReaderException;
	
}
