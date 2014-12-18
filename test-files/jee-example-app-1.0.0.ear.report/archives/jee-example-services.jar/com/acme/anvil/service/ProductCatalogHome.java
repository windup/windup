package com.acme.anvil.service;

import java.rmi.RemoteException;
import javax.ejb.EJBException;
import javax.ejb.CreateException;
import com.acme.anvil.service.ProductCatalog;
import javax.ejb.EJBHome;

public interface ProductCatalogHome extends EJBHome{
    ProductCatalog create() throws CreateException,EJBException,RemoteException;
}
