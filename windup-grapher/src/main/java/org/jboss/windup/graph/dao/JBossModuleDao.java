package org.jboss.windup.graph.dao;

import java.util.Iterator;

import org.jboss.windup.graph.dao.exception.ModuleIndexReaderException;
import org.jboss.windup.graph.dao.exception.ModuleIndexWriteException;
import org.jboss.windup.graph.model.meta.JBossModuleMeta;

public interface JBossModuleDao extends BaseDao<JBossModuleMeta> {
	public void addModule(JBossModuleMeta module) throws ModuleIndexWriteException;
	public Iterator<JBossModuleMeta> findModuleProvidingClass(String clz) throws ModuleIndexReaderException;
}
