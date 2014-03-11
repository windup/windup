package org.jboss.windup.engine.visitor;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.engine.visitor.base.AbstractGraphVisitor;
import org.jboss.windup.graph.dao.ArchiveDaoBean;
import org.jboss.windup.graph.dao.EarArchiveDaoBean;
import org.jboss.windup.graph.dao.JarArchiveDaoBean;
import org.jboss.windup.graph.dao.WarArchiveDaoBean;
import org.jboss.windup.graph.model.resource.ArchiveResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Vertex;

/**
 * Determines the extension of the "unknown" archive type, and then casts it to the 
 * subtype: JAR, WAR, EAR, etc.
 * 
 * @author bradsdavis@gmail.com
 *
 */
public class ArchiveTypingVisitor extends AbstractGraphVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(ArchiveTypingVisitor.class);

	@Inject
	private WarArchiveDaoBean warDao;
	
	@Inject
	private JarArchiveDaoBean jarDao;
	
	@Inject
	private EarArchiveDaoBean earDao;
	
	@Inject
	private ArchiveDaoBean archiveDao;
	
	@Override
	public void run() {
		for(ArchiveResource archive : archiveDao.getAll()) {
			visitArchive(archive);
		}
	}
	
	@Override
	public void visitArchive(ArchiveResource file) {
		//now, check to see whether it is a JAR, and republish the typed value.
		String filePath = file.getFileResource().getFilePath();
		
		if(StringUtils.endsWith(filePath, ".jar")) {
			jarDao.castToType(file);
		}
		else if(StringUtils.endsWith(filePath, ".war")) {
			warDao.castToType(file);
		}
		else if(StringUtils.endsWith(filePath, ".ear")) {
			earDao.castToType(file);
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
