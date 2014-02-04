package org.jboss.windup.engine.visitor;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.jboss.windup.engine.WindupContext;
import org.jboss.windup.engine.visitor.base.EmptyGraphVisitor;
import org.jboss.windup.graph.dao.ArchiveEntryDao;
import org.jboss.windup.graph.dao.JavaClassDao;
import org.jboss.windup.graph.model.resource.Archive;
import org.jboss.windup.graph.model.resource.ArchiveEntryResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For all Java Class entries in the zip, this sets up the JavaClass graph entries. 
 * 
 * @author bradsdavis
 *
 */
public class JavaClassVisitor extends EmptyGraphVisitor {
	private static final Logger LOG = LoggerFactory.getLogger(JavaClassVisitor.class);

	@Inject
	private JavaClassDao javaClassDao;
	
	@Inject
	private ArchiveEntryDao archiveEntryDao;
	
	
	@Override
	public void visitContext(WindupContext context) {
		int i=0;
		for(ArchiveEntryResource entry : archiveEntryDao.findArchiveEntryWithExtension("class")) {
			visitArchiveEntry(entry);
			//for every 1000, commit.
			if((i%1000)==0) {
				javaClassDao.commit();
			}
		}
		javaClassDao.commit();
	}
	
	@Override
	public void visitArchiveEntry(ArchiveEntryResource entry) {
		//now, check to see whether it is a JAR, and republish the typed value.
		Archive archive = entry.getArchive();
		
		if(archive == null) {
			LOG.warn("Archive should not be null: "+entry.asVertex());
			return;
		}
		
		try {
			ClassParser classParser = new ClassParser(archive.getFilePath(), entry.getArchiveEntry());
			
			JavaClass parsed = classParser.parse();
			String className = parsed.getClassName();
			org.jboss.windup.graph.model.resource.JavaClass javaClass = javaClassDao.getJavaClass(className);
			javaClass.addResource(entry);
			
			for(String interfaceName : parsed.getInterfaceNames()) {
				org.jboss.windup.graph.model.resource.JavaClass interfaceClass = javaClassDao.getJavaClass(interfaceName);
				//then we make the connection.
				javaClass.addImplements(interfaceClass);
			}
			
			String superClz = parsed.getSuperclassName();
			org.jboss.windup.graph.model.resource.JavaClass superJavaClass = javaClassDao.getJavaClass(superClz);
			javaClass.setExtends(superJavaClass);
		} catch (IOException e) {
			LOG.error("Exception reading class.", e);
		}
	}
	
	
}
