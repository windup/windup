package org.jboss.windup.tooling.data;

import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface TransformationQuickfixChange extends Remote
{
	/**
	 * The name of this change.
	 */
	void setName(String name) throws RemoteException;
	String getName() throws RemoteException;
	/**
	 * The description of this change.
	 */
	void setDescription(String description) throws RemoteException;
	String getDescription() throws RemoteException;
	/**
	 * The file to be transformed by this change.
	 */
	void setFile(File file) throws RemoteException;
	File getFile() throws RemoteException;
	/**
	 * Returns a preview of what the code will look like if this change were to be applied.
	 */
	String preview() throws RemoteException;
	/**
	 * Applies this change to the underlying file.
	 */
	void apply() throws RemoteException;
	void setLocationData(LocationData data) throws RemoteException;
	LocationData getLocationData() throws RemoteException;
}
