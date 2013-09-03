package org.jboss.windup.classprofiler;

import java.util.Collection;

import org.jboss.windup.classprofiler.exception.ModuleIndexReaderException;
import org.jboss.windup.classprofiler.exception.ModuleIndexWriteException;
import org.jboss.windup.classprofiler.metadata.ModuleVO;

public interface ModuleIndexer {
	public void addModule(ModuleVO module) throws ModuleIndexWriteException;
	public Collection<ModuleVO> findModuleProvidingClass(String clz) throws ModuleIndexReaderException;
}
