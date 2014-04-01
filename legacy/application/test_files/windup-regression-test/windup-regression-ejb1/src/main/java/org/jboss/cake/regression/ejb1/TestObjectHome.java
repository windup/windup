package com.jboss.windup.regression.ejb1;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;
import javax.ejb.FinderException;

public interface TestObjectHome extends EJBHome {
	public TestObject create (String value) throws RemoteException, CreateException;
	public TestObject find (String key) throws RemoteException, FinderException;
}
