package com.acme.anvil.service;

import com.acme.anvil.service.ItemLookup;
import com.acme.anvil.service.jms.LogEventPublisher;
import com.acme.anvil.vo.LogEvent;
import java.util.Date;
import com.acme.anvil.vo.Item;
import org.apache.log4j.Logger;
import javax.ejb.SessionBean;
import weblogic.ejb.GenericSessionBean;

public class ItemLookupBean extends GenericSessionBean implements SessionBean{
    private static final Logger LOG;
    public Item lookupItem(final long id){
        ItemLookupBean.LOG.info((Object)"Calling lookupItem.");
        final Item item=new Item();
        item.setId(id);
        final LogEvent le=new LogEvent(new Date(),"Returning Item: "+id);
        LogEventPublisher.publishLogEvent(le);
        return item;
    }
    static{
        LOG=Logger.getLogger((Class)ItemLookup.class);
    }
}
