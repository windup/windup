package com.acme.anvil.service;

import javax.ejb.EJBException;
import javax.ejb.CreateException;
import com.acme.anvil.service.ItemLookupLocal;
import javax.ejb.EJBLocalHome;

public interface ItemLookupLocalHome extends EJBLocalHome{
    ItemLookupLocal create() throws CreateException,EJBException;
}
