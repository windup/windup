package com.acme.anvil.service;

import java.rmi.RemoteException;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import weblogic.i18n.logging.NonCatalogLogger;
import javax.ejb.SessionBean;

public class ProductCatalogBean implements SessionBean{
    private static final NonCatalogLogger LOG;
    private SessionContext sessionContext;
    public void setSessionContext(final SessionContext ctx) throws EJBException,RemoteException{
        this.sessionContext=this.sessionContext;
    }
    public void ejbRemove() throws EJBException,RemoteException{
        ProductCatalogBean.LOG.info("Called Remove.");
    }
    public void ejbActivate() throws EJBException,RemoteException{
        ProductCatalogBean.LOG.info("Called Activate");
    }
    public void ejbPassivate() throws EJBException,RemoteException{
        ProductCatalogBean.LOG.info("Called Passivate");
    }
    public void populateCatalog(){
        ProductCatalogBean.LOG.info("Do something.");
    }
    static{
        LOG=new NonCatalogLogger("ProductCatalogBean");
    }
}
