package org.jboss.windup.graph.dao;

import java.util.Collection;

import org.jboss.windup.graph.dao.exception.ModuleIndexReaderException;
import org.jboss.windup.graph.dao.exception.ModuleIndexWriteException;
import org.jboss.windup.graph.model.meta.JBossModule;

public interface ModuleDao {
	public void addModule(JBossModule module) throws ModuleIndexWriteException;
	public Collection<JBossModule> findModuleProvidingClass(String clz) throws ModuleIndexReaderException;
}
