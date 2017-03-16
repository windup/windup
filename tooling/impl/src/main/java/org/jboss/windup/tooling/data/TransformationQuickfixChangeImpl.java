package org.jboss.windup.tooling.data;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.function.Supplier;

import org.jboss.windup.tooling.data.TransformationQuickfixChange;

public class TransformationQuickfixChangeImpl extends UnicastRemoteObject implements TransformationQuickfixChange, Remote  
{
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String description;
	private File file;
	private LocationData locationData;
	
	private Runnable applyRunner;
	private Supplier<String> previewRunner;
	
	public TransformationQuickfixChangeImpl (Runnable applyRunner, Supplier<String> previewRunner) throws RemoteException {
		super();
		this.applyRunner = applyRunner;
		this.previewRunner = previewRunner;
	}

	@Override
	public void setName(String name) throws RemoteException 
	{
		this.name = name;
	}

	@Override
	public String getName() throws RemoteException
	{
		return name;
	}

	@Override
	public void setDescription(String description) throws RemoteException
	{
		this.description = description;
	}

	@Override
	public String getDescription() throws RemoteException
	{
		return description;
	}

	@Override
	public void setFile(File file) throws RemoteException
	{
		this.file = file;
	}

	@Override
	public File getFile() throws RemoteException
	{
		return file;
	}

	@Override
	public String preview() throws RemoteException
	{
		return previewRunner.get();
	}

	@Override
	public void apply() throws RemoteException
	{
		applyRunner.run();
	}

	@Override
	public void setLocationData(LocationData locationData) throws RemoteException
	{
		this.locationData = locationData;
	}

	@Override
	public LocationData getLocationData() throws RemoteException
	{
		return locationData;
	}
}
