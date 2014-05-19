package com.jboss.windup.regression.ejb1;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;


public interface TestObject extends EJBObject {
	public int getAttr1() throws RemoteException;
	public int setAttr1() throws RemoteException;
}
