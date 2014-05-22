package com.acme.anvil.service;

import javax.ejb.EJBLocalObject;

import com.acme.anvil.vo.Item;

public interface ItemLookupLocal extends EJBLocalObject {
	public Item lookupItem(long id);
}
