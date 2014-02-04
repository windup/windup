package org.jboss.windup.engine.provider;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.windup.engine.WindupContext;
import org.jboss.windup.engine.qualifier.ArchiveQualifier;
import org.jboss.windup.engine.qualifier.EarQualifier;
import org.jboss.windup.engine.qualifier.WarQualifier;
import org.jboss.windup.graph.dao.ArchiveEntryDaoBean;
import org.jboss.windup.graph.dao.BaseDaoBean;
import org.jboss.windup.graph.dao.DoctypeDaoBean;
import org.jboss.windup.graph.dao.FileDaoBean;
import org.jboss.windup.graph.dao.JarArchiveDaoBean;
import org.jboss.windup.graph.dao.JavaClassDaoBean;
import org.jboss.windup.graph.dao.NamespaceDaoBean;
import org.jboss.windup.graph.dao.XmlResourceDaoBean;
import org.jboss.windup.graph.model.resource.Archive;
import org.jboss.windup.graph.model.resource.EarArchive;
import org.jboss.windup.graph.model.resource.WarArchive;


public class DaoProvider {
	
	@Inject
	private WindupContext context;
	
	@Produces
	public FileDaoBean produceFileDao() {
		return new FileDaoBean(context.getGraphContext());
	}
	
	@Produces
	public JavaClassDaoBean produceJavaClassDao() {
		return new JavaClassDaoBean(context.getGraphContext());
	}
	
	@ArchiveQualifier
	@Produces
	public BaseDaoBean<Archive> produceArchiveDao() {
		return new BaseDaoBean<Archive>(context.getGraphContext(), Archive.class);
	}
	
	@WarQualifier
	@Produces
	public BaseDaoBean<WarArchive> produceWarDao() {
		return new BaseDaoBean<WarArchive>(context.getGraphContext(), WarArchive.class);
	}
	
	@EarQualifier
	@Produces
	public BaseDaoBean<EarArchive> produceEarDao() {
		return new BaseDaoBean<EarArchive>(context.getGraphContext(), EarArchive.class);
	}

	@Produces
	public JarArchiveDaoBean produceJarDao() {
		return new JarArchiveDaoBean(context.getGraphContext());
	}
	
	@Produces
	public ArchiveEntryDaoBean produceArchiveEntryDao() {
		return new ArchiveEntryDaoBean(context.getGraphContext());
	}
	
	@Produces
	public XmlResourceDaoBean produceXmlResourceDao() {
		return new XmlResourceDaoBean(context.getGraphContext());
	}


	@Produces
	public DoctypeDaoBean produceDoctypeDao() {
		return new DoctypeDaoBean(context.getGraphContext());
	}
	
	
	@Produces
	public NamespaceDaoBean produceNamespaceDao() {
		return new NamespaceDaoBean(context.getGraphContext());
	}
	
}