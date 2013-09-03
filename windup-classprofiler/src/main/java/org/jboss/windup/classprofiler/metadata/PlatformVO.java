package org.jboss.windup.classprofiler.metadata;

/**
 * Describes the platform.  Example: JBoss EAP 6.0.1
 * 
 * @author bradsdavis
 *
 */
public class PlatformVO {

	private String name;
	private String version;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
	@Override
	public String toString() {
		return "PlatformVO [name=" + name + ", version=" + version + "]";
	}
}
