package com.jboss.windup.regression.ejb1;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.RemoveException;

public class TestObjectBean implements EntityBean {
	public String key;
	public String value;
	
	public TestObjectBean(){
		
	}

	@Override
	public void ejbActivate() throws EJBException, RemoteException {
		
	}

	@Override
	public void ejbLoad() throws EJBException, RemoteException {
		
	}

	@Override
	public void ejbPassivate() throws EJBException, RemoteException {
		
	}

	@Override
	public void ejbRemove() throws RemoveException, EJBException,
			RemoteException {
		
	}

	@Override
	public void ejbStore() throws EJBException, RemoteException {
		
	}

	@Override
	public void setEntityContext(EntityContext arg0) throws EJBException,
			RemoteException {
		
	}

	@Override
	public void unsetEntityContext() throws EJBException, RemoteException {
		
	}
	
	
}
