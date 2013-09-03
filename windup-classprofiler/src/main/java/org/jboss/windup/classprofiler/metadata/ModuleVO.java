package org.jboss.windup.classprofiler.metadata;

import java.util.LinkedList;
import java.util.List;

/**
 * Describes the named JBoss Module.  Example: name="org.jboss.as.console" slot="eap"
 * 
 * @author bradsdavis
 *
 */
public class ModuleVO {
	PlatformVO platform;
	
	private String name = "main";
	private String slot;
	
	/**
	 * Provides a link to all module dependency by names. 
	 */
	private List<ModuleVO> dependencies = new LinkedList<ModuleVO>();
	private List<ArchiveVO> archives = new LinkedList<ArchiveVO>();

	public List<ArchiveVO> getArchives() {
		return archives;
	}
	
	public List<ModuleVO> getDependencies() {
		return dependencies;
	}
	
	public void setPlatform(PlatformVO platform) {
		this.platform = platform;
	}
	
	public PlatformVO getPlatform() {
		return platform;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSlot() {
		return slot;
	}

	public void setSlot(String slot) {
		this.slot = slot;
	}

	@Override
	public String toString() {
		return "ModuleVO [platform=" + platform + ", name=" + name + ", slot="
				+ slot + ", dependencies=" + dependencies + ", archives="
				+ archives + "]";
	}

	
}
