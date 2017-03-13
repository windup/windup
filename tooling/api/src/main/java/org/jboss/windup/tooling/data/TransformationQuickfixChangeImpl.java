package org.jboss.windup.tooling.data;

import java.io.File;

import org.jboss.windup.tooling.data.TransformationQuickfixChange;

public class TransformationQuickfixChangeImpl implements TransformationQuickfixChange 
{
	private String name;
	private String description;
	private File file;
	private String snippet;
	int lineNumber;
	int startPosition;
	int length;

	@Override
	public void setName(String name) 
	{
		this.name = name;
	}

	@Override
	public String getName() 
	{
		return name;
	}

	@Override
	public void setDescription(String description) 
	{
		this.description = description;
	}

	@Override
	public String getDescription() 
	{
		return description;
	}

	@Override
	public void setFile(File file) 
	{
		this.file = file;
	}

	@Override
	public File getFile() 
	{
		return file;
	}

	@Override
	public String preview() 
	{
		return "";
	}

	@Override
	public void apply() 
	{
	}

	@Override
	public void setSnippet(String snippet) 
	{
		this.snippet = snippet;
	}

	@Override
	public String getSnippet() 
	{
		return snippet;
	}

	@Override
	public void setLineNumber(int lineNumber) 
	{
		this.lineNumber = lineNumber;
	}

	@Override
	public int getLineNumber() 
	{
		return lineNumber;
	}

	@Override
	public void setStartPosition(int startPosition) 
	{
		this.startPosition = startPosition;
	}

	@Override
	public int getStartPosition() 
	{
		return startPosition;
	}

	@Override
	public void setLength(int length) 
	{
		this.length = length;
	}

	@Override
	public int getLength() 
	{
		return length;
	}
}
