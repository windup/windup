package com.acme.anvil.service;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import weblogic.i18n.logging.NonCatalogLogger;

public class ProductCatalogBean implements SessionBean {

	private static final NonCatalogLogger LOG = new NonCatalogLogger("ProductCatalogBean");
	
	private SessionContext sessionContext;
	
	public void setSessionContext(SessionContext ctx) throws EJBException, RemoteException {
		this.sessionContext = sessionContext;
	}

	public void ejbRemove() throws EJBException, RemoteException {
		LOG.info("Called Remove.");
	}

	public void ejbActivate() throws EJBException, RemoteException {
		LOG.info("Called Activate");
	}

	public void ejbPassivate() throws EJBException, RemoteException {
		LOG.info("Called Passivate");
	}
	
	public void populateCatalog() {
		LOG.info("Do something.");
	}
}
