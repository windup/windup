package com.rhc.booking.services.impl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.rhc.booking.services.EventServer;

public class EventServerImpl extends UnicastRemoteObject implements EventServer{

	public EventServerImpl() throws RemoteException {
		
	}
	
	public String processEvent(String event) throws RemoteException {
		return "Processed: "+event;
	}
}
