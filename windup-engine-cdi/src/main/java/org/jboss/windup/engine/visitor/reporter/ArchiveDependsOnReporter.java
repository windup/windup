package org.jboss.windup.engine.visitor.reporter;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.jboss.windup.graph.dao.JarArchiveDaoBean;
import org.jboss.windup.graph.model.resource.JarArchive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For each JAR, this displays the JARs that this JAR depends on.
 * This is not transitive.
 * 
 * Example:
 * Class A is in JAR W
 * Class B is in JAR X
 * Class C is in JAR Y
 * Class D is in JAR Z
 * 
 * Class A extends Class B
 * Class C implements Class D
 * 
 * JAR W Depends on JAR X
 * JAR Y Depends on JAR Z
 * 
 * JAR X Provides for JAR W
 * JAR Z Provides for JAR Y
 * 
 * @author bradsdavis
 *
 */
public class ArchiveDependsOnReporter extends EmptyGraphVisitor {

	private static final Logger LOG = LoggerFactory.getLogger(ArchiveDependsOnReporter.class);
	
	@Inject
	private JarArchiveDaoBean jarDao;
	
	@Override
	public void visit() {
		for(JarArchive archive : jarDao.getAll()) {
			LOG.info("Archive: "+archive.getFilePath());
			
			for(JarArchive clz : archive.dependsOnArchives()) {
				LOG.info(" - Depends On: "+clz.getFilePath());
			}

			for(JarArchive clz : archive.providesForArchives()) {
				LOG.info(" - Provides For: "+clz.getFilePath());
			}
		}
	}
}
