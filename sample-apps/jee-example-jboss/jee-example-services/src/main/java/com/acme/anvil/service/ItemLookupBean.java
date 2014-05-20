package com.acme.anvil.service;

import com.acme.anvil.service.jms.LogEventPublisher;
import com.acme.anvil.vo.Item;
import com.acme.anvil.vo.LogEvent;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.transaction.InvalidTransactionException;
import javax.transaction.SystemException;
import org.apache.log4j.Logger;
import org.jboss.ejb3.annotation.TransactionTimeout;


@TransactionTimeout(value = 180, unit = TimeUnit.SECONDS)
public class ItemLookupBean implements SessionBean {

	private static final Logger LOG = Logger.getLogger(ItemLookup.class);
	
    @EJB LogEventPublisher publisher;
	public Item lookupItem(long id) throws SystemException, InvalidTransactionException {
		LOG.info("Calling lookupItem.");
		
		//stubbed out.
		Item item = new Item();
		item.setId(id);
		
		final LogEvent le = new LogEvent(new Date(), "Returning Item: "+id); 
		publisher.publishLogEvent(le);
		
		return item;
	}


    public void setSessionContext( SessionContext sc ) throws EJBException, RemoteException {
    }


    public void ejbRemove() throws EJBException, RemoteException {
    }


    public void ejbActivate() throws EJBException, RemoteException {
    }


    public void ejbPassivate() throws EJBException, RemoteException {
    }
}
