package org.jboss.windup.tooling.rules;

import java.io.Serializable;

public interface Technology extends Serializable 
{
	int getVersion();
	void setVersion(int version);
    /**
     * Contains the name of the technology (for example, 'eap').
     */
    String getName();
    /**
     * Contains the name of the technology (for example, 'eap').
     */
    void setName(String name);
    /**
     * Contains the version range of the technology (for example, '[6]').
     */
    String getVersionRange();
    /**
     * Contains the version range of the technology (for example, '[6]').
     */
    void setVersionRange(String versionRange);
}