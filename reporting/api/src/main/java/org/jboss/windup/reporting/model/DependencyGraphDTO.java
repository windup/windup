package org.jboss.windup.reporting.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.jboss.windup.graph.model.ProjectModel;

public class DependencyGraphDTO {

	private String sha1;

	/** jar, war, ear, etc. **/
	private String type;

	private String kind;

	private String name;

	private Set<String> parents = new HashSet<>();

	public DependencyGraphDTO(final ProjectModel projectModel, boolean isChildren, boolean isSkipped) {
		this.sha1 = projectModel.getRootFileModel().getSHA1Hash();
		this.name = projectModel.getRootFileModel().getFileName();
		if (projectModel.getProjectType() == null) {
			// sometimes the type is resolved to null
			this.type = FilenameUtils.getExtension(this.name);
		} else {
			this.type = projectModel.getProjectType();
		}
		this.kind = Kind.getKindByType(type, isChildren, isSkipped).getValue();
	}

	public String getSha1() {
		return sha1;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getKind() {
		return this.kind;
	}

	public Set<String> getParents() {
		return Collections.unmodifiableSet(parents);
	}

	public void addParent(final String parent) {
		parents.add(parent);
	}

	enum Kind {

		EAR("Ear"), WAR_APP("WarApp"), WAR("War"), JAR("Jar"), EXTERNAL_JAR("ExternalJar"), UNKNOWN("unknown");

		private String value;

		Kind(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		static Kind getKindByType(final String type, final boolean isChildren, final boolean isSkipped) {
			switch (type.toLowerCase()) {
			case "jar":
				return isSkipped ? EXTERNAL_JAR : JAR;
			case "war":
				return isChildren ? WAR : WAR_APP;
			case "ear":
				return EAR;
			default:
				return UNKNOWN; // should never happen, but who knows...
			}
		}

	}
}