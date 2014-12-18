package com.acme.anvil.service;

import com.acme.anvil.vo.Item;
import javax.ejb.EJBLocalObject;

public interface ItemLookupLocal extends EJBLocalObject{
    Item lookupItem(long p0);
}
