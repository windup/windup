package com.rhc.booking.services;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface EventServer extends Remote {
   String processEvent(String var1) throws RemoteException;
}
