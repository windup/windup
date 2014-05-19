package com.acme.anvil.service;

import java.rmi.RemoteException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import org.apache.log4j.Logger;

public class ProductCatalogBean implements SessionBean {

	private static final Logger LOG = Logger.getLogger(ProductCatalogBean.class);
	
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
