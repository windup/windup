package org.jboss.windup.graph.dao;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

import org.jboss.windup.graph.dao.exception.ArchiveIndexReaderException;
import org.jboss.windup.graph.model.resource.JarArchive;

public class JarArchiveDaoBean extends BaseDaoBean<JarArchive> {

	public JarArchiveDaoBean() {
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

	public JarFile asJarFile(JarArchive archive) throws IOException {
		File file = new File(archive.getFileResource().getFilePath());
		return new JarFile(file);
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
			set.add(d.getFileResource().getFilePath());
		}
		for(JarArchive p : archive.providesForArchives()) {
			if(set.contains(p.getFileResource().getFilePath())) {
				results.add(p);
			}
		}
			
		return results;
	}
	
	
}
