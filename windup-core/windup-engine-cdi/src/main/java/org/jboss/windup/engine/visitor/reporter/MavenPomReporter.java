package org.jboss.windup.engine.visitor.reporter;

import javax.inject.Inject;

import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.jboss.windup.graph.dao.MavenFacetDaoBean;
import org.jboss.windup.graph.model.meta.xml.MavenFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shows Maven POM information.
 * 
 * @author bradsdavis@gmail.com
 *
 */
public class MavenPomReporter extends EmptyGraphVisitor {

	private static final Logger LOG = LoggerFactory.getLogger(MavenPomReporter.class);
	
	@Inject
	private MavenFacetDaoBean mavenDao;
	
	@Override
	public void run() {
		for(MavenFacet facet : mavenDao.getAll()) {
			LOG.info("Maven: "+facet.getGroupId()+":"+facet.getArtifactId()+":"+facet.getVersion());
			
			if(facet.getParent() != null) {
				MavenFacet parent = facet.getParent();
				LOG.info(" - Parent: "+parent.getGroupId()+":"+parent.getArtifactId()+":"+parent.getVersion());
			}
			
			
			for(MavenFacet child : facet.getChildModules()) {
				//report the xml files that contain the namespace...
				LOG.info(" - Module: "+child.getGroupId()+":"+child.getArtifactId()+":"+child.getVersion());
			}
			
			for(MavenFacet dep : facet.getDependencies()) {
				//report the xml files that contain the namespace...
				LOG.info(" - Dependency: "+dep.getGroupId()+":"+dep.getArtifactId()+":"+dep.getVersion());
			}
			
		}
	}
}
