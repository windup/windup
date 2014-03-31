/*
 * Copyright (c) 2013 Red Hat, Inc. and/or its affiliates.
 *  
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  Contributors:
 *      Brad Davis - bradsdavis@gmail.com - Initial API and implementation
*/
package org.jboss.windup.metadata.type.archive;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.windup.metadata.type.FileMetadata;


public abstract class ArchiveMetadata extends FileMetadata {
	//TODO: Move the context out to be passed along outside of archivemeta.
	private Map<String, Object> context = new HashMap<String, Object>();
	
	public Map<String, Object> getContext() {
		return context;
	}
	
	private Set<FileMetadata> entries = new HashSet<FileMetadata>();
	private Set<ArchiveMetadata> nestedArchives = new HashSet<ArchiveMetadata>();

	private String relativePath;
	private String name;
	
	protected File archiveOutputDirectory;

	public Set<ArchiveMetadata> getNestedArchives() {
		return nestedArchives;
	}

	public Set<FileMetadata> getEntries() {
		return entries;
	}

	public void setEntries(Set<FileMetadata> entries) {
		this.entries = entries;
	}

	public File getArchiveOutputDirectory() {
		return archiveOutputDirectory;
	}

	public void setArchiveOutputDirectory(File archiveOutputDirectory) {
		this.archiveOutputDirectory = archiveOutputDirectory;
	}


	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "ArchiveMetadata [filePointer=" + filePointer + ", relativePath=" + relativePath + ", name=" + name + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((relativePath == null) ? 0 : relativePath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArchiveMetadata other = (ArchiveMetadata) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		if (relativePath == null) {
			if (other.relativePath != null)
				return false;
		}
		else if (!relativePath.equals(other.relativePath))
			return false;
		return true;
	}
}
