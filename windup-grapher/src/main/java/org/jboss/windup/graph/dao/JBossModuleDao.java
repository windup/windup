package org.jboss.windup.graph.dao;

import java.util.Iterator;

import org.jboss.windup.graph.dao.exception.ModuleIndexReaderException;
import org.jboss.windup.graph.dao.exception.ModuleIndexWriteException;
import org.jboss.windup.graph.model.meta.JBossModule;

public interface JBossModuleDao extends BaseDao<JBossModule> {
	public void addModule(JBossModule module) throws ModuleIndexWriteException;
	public Iterator<JBossModule> findModuleProvidingClass(String clz) throws ModuleIndexReaderException;
}
