package org.apache.wicket.pageStore.memory;

import org.apache.wicket.pageStore.*;
import org.apache.wicket.page.*;
import org.apache.wicket.*;
import java.io.*;
import org.slf4j.*;

public class HttpSessionDataStore implements IDataStore{
    private static final Logger log;
    private static final String PAGE_TABLE_KEY="page:store:memory";
    private final IPageManagerContext pageManagerContext;
    private final DataStoreEvictionStrategy evictionStrategy;
    public HttpSessionDataStore(final IPageManagerContext pageManagerContext,final DataStoreEvictionStrategy evictionStrategy){
        super();
        this.pageManagerContext=pageManagerContext;
        this.evictionStrategy=evictionStrategy;
    }
    public byte[] getData(final String sessionId,final int pageId){
        final PageTable pageTable=this.getPageTable(false);
        byte[] pageAsBytes=null;
        if(pageTable!=null){
            pageAsBytes=pageTable.getPage(pageId);
        }
        return pageAsBytes;
    }
    public void removeData(final String sessionId,final int pageId){
        final PageTable pageTable=this.getPageTable(false);
        if(pageTable!=null){
            pageTable.removePage(pageId);
        }
    }
    public void removeData(final String sessionId){
        final PageTable pageTable=this.getPageTable(false);
        if(pageTable!=null){
            pageTable.clear();
        }
    }
    public void storeData(final String sessionId,final int pageId,final byte[] pageAsBytes){
        final PageTable pageTable=this.getPageTable(true);
        if(pageTable!=null){
            pageTable.storePage(pageId,pageAsBytes);
            this.evictionStrategy.evict(pageTable);
        }
        else{
            HttpSessionDataStore.log.error("Cannot store the data for page with id '{}' in session with id '{}'",(Object)pageId,sessionId);
        }
    }
    public void destroy(){
    }
    public boolean isReplicated(){
        return true;
    }
    private PageTable getPageTable(final boolean create){
        PageTable pageTable=null;
        if(Session.exists()){
            pageTable=(PageTable)this.pageManagerContext.getSessionAttribute("page:store:memory");
            if(pageTable==null&&create){
                pageTable=new PageTable();
                this.pageManagerContext.setSessionAttribute("page:store:memory",(Serializable)pageTable);
            }
        }
        return pageTable;
    }
    static{
        log=LoggerFactory.getLogger(HttpSessionDataStore.class);
    }
}
