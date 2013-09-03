package org.jboss.windup;

import java.util.Collection;

import org.jboss.windup.exception.ModuleIndexReaderException;
import org.jboss.windup.exception.ModuleIndexWriteException;
import org.jboss.windup.metadata.ModuleVO;

public interface ModuleIndexer {
	public void addModule(ModuleVO module) throws ModuleIndexWriteException;
	public Collection<ModuleVO> findModuleProvidingClass(String clz) throws ModuleIndexReaderException;
}
