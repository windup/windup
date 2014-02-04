package org.jboss.windup.graph.dao.impl;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.dao.JarArchiveDao;
import org.jboss.windup.graph.dao.exception.ArchiveIndexReaderException;
import org.jboss.windup.graph.model.resource.JarArchive;

public class JarArchiveDaoImpl extends BaseDaoImpl<JarArchive> implements JarArchiveDao {

	public JarArchiveDaoImpl(GraphContext context) {
		super(context, JarArchive.class);
	}

	@Override
	public Iterable<JarArchive> findArchiveByMD5(String value) throws ArchiveIndexReaderException {
		return this.getByProperty("md5Hash", value);
	}

	@Override
	public Iterable<JarArchive> findArchiveBySHA1(String value) throws ArchiveIndexReaderException {
		return this.getByProperty("sha1Hash", value);
	}

	@Override
	public Iterable<JarArchive> findArchiveByName(String value) throws ArchiveIndexReaderException {
		return this.getByProperty("archiveName", value);
	}
	
	@Override
	public Iterable<JarArchive> findArchiveByQualifiedClassName(String clz) throws ArchiveIndexReaderException {
		return null;
	}

	@Override
	public JarFile asJarFile(JarArchive archive) throws IOException {
		File file = new File(archive.getFilePath());
		return new JarFile(file);
	}

	
	
}
