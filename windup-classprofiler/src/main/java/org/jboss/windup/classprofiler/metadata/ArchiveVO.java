package org.jboss.windup.classprofiler.metadata;

import java.util.HashMap;
import java.util.Map;

public class ArchiveVO {
	
	private String name;
	private String version;
	private String md5;
	private String sha1;
	
	private Map<String, String> properties = new HashMap<String, String>();
	
	public Map<String, String> getProperties() {
		return properties;
	}
	
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
	
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	public String getSha1() {
		return sha1;
	}
	public void setSha1(String sha1) {
		this.sha1 = sha1;
	}
	
	@Override
	public String toString() {
		return "ArchiveVO [name=" + name + ", version=" + version + ", md5="
				+ md5 + ", sha1=" + sha1 + "]";
	}	
}
