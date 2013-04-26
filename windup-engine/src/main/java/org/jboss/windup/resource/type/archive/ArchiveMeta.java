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
package org.jboss.windup.resource.type.archive;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.windup.resource.type.FileMeta;


public abstract class ArchiveMeta extends FileMeta {
	//TODO: Move the context out to be passed along outside of archivemeta.
	private Map<String, Object> context = new HashMap<String, Object>();
	
	public Map<String, Object> getContext() {
		return context;
	}
	
	private Set<FileMeta> entries = new HashSet<FileMeta>();
	private Set<ArchiveMeta> nestedArchives = new HashSet<ArchiveMeta>();

	private String relativePath;
	private String name;
	
	protected File archiveOutputDirectory;

	public Set<ArchiveMeta> getNestedArchives() {
		return nestedArchives;
	}

	public Set<FileMeta> getEntries() {
		return entries;
	}

	public void setEntries(Set<FileMeta> entries) {
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
		return "ArchiveMeta [filePointer=" + filePointer + ", relativePath=" + relativePath + ", name=" + name + "]";
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
		ArchiveMeta other = (ArchiveMeta) obj;
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
