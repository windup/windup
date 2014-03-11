package org.jboss.windup.graph.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jboss.windup.graph.model.resource.ArchiveResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArchiveDaoBean extends BaseDaoBean<ArchiveResource> {

	private static Logger LOG = LoggerFactory.getLogger(ArchiveDaoBean.class);
	
	public File asFile(ArchiveResource resource) {
		return new File(resource.getFileResource().getFilePath());
	}
	

	public InputStream getPayload(ArchiveResource file) throws IOException {
		String fileName = file.getFileResource().getFilePath();
		FileInputStream fis = new FileInputStream(new java.io.File(fileName));
		return fis;
	}
	
	public ArchiveDaoBean() {
		super(ArchiveResource.class);
	}
}
