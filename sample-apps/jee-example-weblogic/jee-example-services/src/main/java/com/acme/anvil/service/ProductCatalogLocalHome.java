package com.acme.anvil.service;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalHome;

public interface ProductCatalogLocalHome extends EJBLocalHome {
	ProductCatalogLocal create() throws CreateException, EJBException;
}
