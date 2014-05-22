package com.acme.anvil.service;

import java.util.Date;

import javax.ejb.SessionBean;

import org.apache.log4j.Logger;

import weblogic.ejb.GenericSessionBean;

import com.acme.anvil.service.jms.LogEventPublisher;
import com.acme.anvil.vo.Item;
import com.acme.anvil.vo.LogEvent;

public class ItemLookupBean extends GenericSessionBean implements SessionBean {

	private static final Logger LOG = Logger.getLogger(ItemLookup.class);
	
	public Item lookupItem(long id) {
		LOG.info("Calling lookupItem.");
		
		//stubbed out.
		Item item = new Item();
		item.setId(id);
		
		final LogEvent le = new LogEvent(new Date(), "Returning Item: "+id); 
		LogEventPublisher.publishLogEvent(le);
		
		return item;
	}
}
