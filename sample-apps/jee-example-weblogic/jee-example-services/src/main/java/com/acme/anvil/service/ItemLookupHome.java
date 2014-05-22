package com.acme.anvil.service;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;

public interface ItemLookupHome extends EJBHome {
	ItemLookup create() throws CreateException, EJBException, RemoteException;
}
