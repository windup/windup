package org.jboss.windup.tooling.data;

import java.io.File;
import java.io.Serializable;

public interface TransformationQuickfixChange extends Serializable
{
	/**
	 * The name of this change.
	 */
	void setName(String name);
	String getName();
	/**
	 * The description of this change.
	 */
	void setDescription(String description);
	String getDescription();
	/**
	 * The file to be transformed by this change.
	 */
	void setFile(File file);
	File getFile();
	/**
	 * Returns a preview of what the code will look like if this change were to be applied.
	 */
	String preview();
	/**
	 * Applies this change to the underlying file.
	 */
	void apply();
	void setLocationData(LocationData data);
	LocationData getLocationData();
}
