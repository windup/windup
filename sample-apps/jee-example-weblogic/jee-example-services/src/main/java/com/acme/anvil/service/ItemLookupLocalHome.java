package com.acme.anvil.service;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalHome;

public interface ItemLookupLocalHome extends EJBLocalHome {
	ItemLookupLocal create() throws CreateException, EJBException;
}
