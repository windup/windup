package org.jboss.windup.tooling.data;

import java.io.File;

import org.jboss.windup.tooling.data.TransformationQuickfixChange;

public class TransformationQuickfixChangeImpl implements TransformationQuickfixChange 
{
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String description;
	private File file;
	private LocationData locationData;

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
	public void setLocationData(LocationData locationData) 
	{
		this.locationData = locationData;
	}

	@Override
	public LocationData getLocationData() 
	{
		return locationData;
	}
}
