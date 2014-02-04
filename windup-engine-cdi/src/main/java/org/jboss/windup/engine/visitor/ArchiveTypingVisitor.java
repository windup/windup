package org.jboss.windup.engine.visitor;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.engine.qualifier.ArchiveQualifier;
import org.jboss.windup.engine.qualifier.EarQualifier;
import org.jboss.windup.engine.qualifier.WarQualifier;
import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.jboss.windup.graph.dao.BaseDaoBean;
import org.jboss.windup.graph.dao.JarArchiveDaoBean;
import org.jboss.windup.graph.model.resource.Archive;
import org.jboss.windup.graph.model.resource.EarArchive;
import org.jboss.windup.graph.model.resource.JarArchive;
import org.jboss.windup.graph.model.resource.WarArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Vertex;

/**
 * Determines the extension of the "unknown" archive type, and then casts it to the 
 * subtype: JAR, WAR, EAR, etc.
 * 
 * @author bradsdavis
 *
 */
public class ArchiveTypingVisitor extends EmptyGraphVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(ArchiveTypingVisitor.class);

	@WarQualifier
	@Inject
	private BaseDaoBean<WarArchive> warDao;
	
	@Inject
	private JarArchiveDaoBean jarDao;
	
	@EarQualifier
	@Inject
	private BaseDaoBean<EarArchive> earDao;
	
	@ArchiveQualifier
	@Inject
	private BaseDaoBean<Archive> archiveDao;
	
	
	@Override
	public void visit() {
		for(Archive archive : archiveDao.getAll()) {
			visitArchive(archive);
		}
	}
	
	@Override
	public void visitArchive(Archive file) {
		LOG.info("Vertex: "+file.asVertex());
		//now, check to see whether it is a JAR, and republish the typed value.
		String filePath = file.getFilePath();
		
		if(StringUtils.endsWith(filePath, ".jar")) {
			JarArchive archive = jarDao.castToType(file);
			LOG.info(" - as JAR");
		}
		else if(StringUtils.endsWith(filePath, ".war")) {
			WarArchive archive = warDao.castToType(file);
			LOG.info(" - as WAR");
		}
		else if(StringUtils.endsWith(filePath, ".ear")) {
			EarArchive archive = earDao.castToType(file);
			LOG.info(" - as EAR");
		}
		else {
			Vertex v = file.asVertex();
			LOG.info("Not found for Vertex: "+v);
			for(String key : v.getPropertyKeys()) {
				LOG.info(" - "+key+" -> "+v.getProperty(key));
			}
			
			
			LOG.warn("Extension not routed for: "+filePath);
		}
	}
	
}
