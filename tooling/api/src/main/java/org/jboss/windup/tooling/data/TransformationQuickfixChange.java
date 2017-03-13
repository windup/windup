package org.jboss.windup.tooling.data;

import java.io.File;

public interface TransformationQuickfixChange 
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
	/**
	 * The snippet of code to be transformed.
	 */
	void setSnippet(String snippet);
	String getSnippet();
	/**
	 * The line number at which this change starts.
	 */
	void setLineNumber(int lineNumber);
	int getLineNumber();
	/**
	 * The starting position (column) of this change.
	 */
	void setStartPosition(int startPosition);
	int getStartPosition();
	/**
	 * The length of the snippet of code this change encapsulates.
	 */
	void setLength(int length);
	int getLength();
}
