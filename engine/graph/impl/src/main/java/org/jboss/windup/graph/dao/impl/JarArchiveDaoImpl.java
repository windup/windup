package org.jboss.windup.graph.dao.impl;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import org.jboss.windup.graph.dao.JarArchiveDao;
import org.jboss.windup.graph.dao.exception.ArchiveIndexReaderException;
import org.jboss.windup.graph.model.resource.JarArchive;

@Singleton
public class JarArchiveDaoImpl extends BaseDaoImpl<JarArchive> implements JarArchiveDao {

	public JarArchiveDaoImpl() {
		super(JarArchive.class);
	}

	public Iterable<JarArchive> findArchiveByMD5(String value) throws ArchiveIndexReaderException {
		return this.getByProperty("md5Hash", value);
	}

	public Iterable<JarArchive> findArchiveBySHA1(String value) throws ArchiveIndexReaderException {
		return this.getByProperty("sha1Hash", value);
	}

	public Iterable<JarArchive> findArchiveByName(String value) throws ArchiveIndexReaderException {
		return this.getByProperty("archiveName", value);
	}
	
	public Iterable<JarArchive> findArchiveByQualifiedClassName(String clz) throws ArchiveIndexReaderException {
		return null;
	}

	public Iterable<JarArchive> findUnusedJars() {
		List<JarArchive> iterable = new LinkedList<JarArchive>();
		for(JarArchive archive : getAll()) {
			if(archive.providesForArchives().iterator().hasNext()) {
				continue;
			}
			iterable.add(archive);
		}
		
		return iterable;
	}

	public Iterable<JarArchive> findCircularReferences(JarArchive archive) {
		Set<JarArchive> results = new HashSet<>();

		//if it is both providing for and depending on, is circular.
		Set<String> set = new HashSet<>();
		for(JarArchive d : archive.dependsOnArchives()) {
			set.add(d.asFile().getAbsolutePath());
		}
		for(JarArchive p : archive.providesForArchives()) {
			if(set.contains(p.asFile().getAbsolutePath())) {
				results.add(p);
			}
		}
			
		return results;
	}
	
	
}
