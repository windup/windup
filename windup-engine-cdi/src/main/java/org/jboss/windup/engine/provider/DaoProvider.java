package org.jboss.windup.engine.provider;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.windup.engine.WindupContext;
import org.jboss.windup.engine.qualifier.ArchiveQualifier;
import org.jboss.windup.engine.qualifier.EarQualifier;
import org.jboss.windup.engine.qualifier.WarQualifier;
import org.jboss.windup.graph.dao.ArchiveEntryDao;
import org.jboss.windup.graph.dao.BaseDao;
import org.jboss.windup.graph.dao.DoctypeDao;
import org.jboss.windup.graph.dao.FileDao;
import org.jboss.windup.graph.dao.JarArchiveDao;
import org.jboss.windup.graph.dao.JavaClassDao;
import org.jboss.windup.graph.dao.NamespaceDao;
import org.jboss.windup.graph.dao.XmlResourceDao;
import org.jboss.windup.graph.dao.impl.ArchiveEntryDaoImpl;
import org.jboss.windup.graph.dao.impl.BaseDaoImpl;
import org.jboss.windup.graph.dao.impl.DoctypeDaoImpl;
import org.jboss.windup.graph.dao.impl.FileDaoImpl;
import org.jboss.windup.graph.dao.impl.JarArchiveDaoImpl;
import org.jboss.windup.graph.dao.impl.JavaClassDaoImpl;
import org.jboss.windup.graph.dao.impl.NamespaceDaoImpl;
import org.jboss.windup.graph.dao.impl.XmlResourceDaoImpl;
import org.jboss.windup.graph.model.resource.Archive;
import org.jboss.windup.graph.model.resource.EarArchive;
import org.jboss.windup.graph.model.resource.WarArchive;


public class DaoProvider {
	
	@Inject
	private WindupContext context;
	
	@Produces
	public FileDao produceFileDao() {
		return new FileDaoImpl(context.getGraphContext());
	}
	
	@Produces
	public JavaClassDao produceJavaClassDao() {
		return new JavaClassDaoImpl(context.getGraphContext());
	}
	
	@ArchiveQualifier
	@Produces
	public BaseDao<Archive> produceArchiveDao() {
		return new BaseDaoImpl<Archive>(context.getGraphContext(), Archive.class);
	}
	
	@WarQualifier
	@Produces
	public BaseDao<WarArchive> produceWarDao() {
		return new BaseDaoImpl<WarArchive>(context.getGraphContext(), WarArchive.class);
	}
	
	@EarQualifier
	@Produces
	public BaseDao<EarArchive> produceEarDao() {
		return new BaseDaoImpl<EarArchive>(context.getGraphContext(), EarArchive.class);
	}

	@Produces
	public JarArchiveDao produceJarDao() {
		return new JarArchiveDaoImpl(context.getGraphContext());
	}
	
	@Produces
	public ArchiveEntryDao produceArchiveEntryDao() {
		return new ArchiveEntryDaoImpl(context.getGraphContext());
	}
	
	@Produces
	public XmlResourceDao produceXmlResourceDao() {
		return new XmlResourceDaoImpl(context.getGraphContext());
	}


	@Produces
	public DoctypeDao produceDoctypeDao() {
		return new DoctypeDaoImpl(context.getGraphContext());
	}
	
	
	@Produces
	public NamespaceDao produceNamespaceDao() {
		return new NamespaceDaoImpl(context.getGraphContext());
	}
	
}