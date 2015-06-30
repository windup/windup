package com.rhc.booking.services;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface EventServer extends Remote {
	
	public String processEvent(String event) throws RemoteException;
	
}
