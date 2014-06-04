package org.apache.wicket.pageStore;

import java.util.*;
import java.util.concurrent.*;
import org.slf4j.*;
import org.apache.wicket.util.lang.*;

public class AsynchronousDataStore implements IDataStore{
    private static final Logger log;
    private static final long OFFER_WAIT=30L;
    private static final long POLL_WAIT=1000L;
    private final Thread pageSavingThread;
    private final IDataStore dataStore;
    private final BlockingQueue<Entry> entries;
    private final ConcurrentMap<String,Entry> entryMap;
    public AsynchronousDataStore(final IDataStore dataStore,final int capacity){
        super();
        this.dataStore=dataStore;
        this.entries=new LinkedBlockingQueue<Entry>(capacity);
        this.entryMap=new ConcurrentHashMap<String,Entry>();
        final PageSavingRunnable savingRunnable=new PageSavingRunnable(dataStore,(BlockingQueue)this.entries,(ConcurrentMap)this.entryMap);
        (this.pageSavingThread=new Thread(savingRunnable,"Wicket-PageSavingThread")).setDaemon(true);
        this.pageSavingThread.start();
    }
    public void destroy(){
        if(this.pageSavingThread.isAlive()){
            this.pageSavingThread.interrupt();
            try{
                this.pageSavingThread.join();
            }
            catch(InterruptedException e){
                AsynchronousDataStore.log.error(e.getMessage(),e);
            }
        }
        this.dataStore.destroy();
    }
    private Entry getEntry(final String sessionId,final int id){
        return (Entry)this.entryMap.get(getKey(sessionId,id));
    }
    public byte[] getData(final String sessionId,final int id){
        final Entry entry=this.getEntry(sessionId,id);
        if(entry!=null){
            AsynchronousDataStore.log.debug("Returning the data of a non-stored entry with sessionId '{}' and pageId '{}'",sessionId,id);
            return entry.data;
        }
        final byte[] data=this.dataStore.getData(sessionId,id);
        AsynchronousDataStore.log.debug("Returning the data of a stored entry with sessionId '{}' and pageId '{}'",sessionId,id);
        return data;
    }
    public boolean isReplicated(){
        return this.dataStore.isReplicated();
    }
    public void removeData(final String sessionId,final int id){
        final String key=getKey(sessionId,id);
        if(key!=null){
            final Entry entry=(Entry)this.entryMap.remove(key);
            if(entry!=null){
                this.entries.remove(entry);
            }
        }
        this.dataStore.removeData(sessionId,id);
    }
    public void removeData(final String sessionId){
        final Iterator<Entry> itor=(Iterator<Entry>)this.entries.iterator();
        while(itor.hasNext()){
            final Entry entry=(Entry)itor.next();
            if(entry!=null){
                final String entrySessionId=entry.sessionId;
                if(!sessionId.equals(entrySessionId)){
                    continue;
                }
                this.entryMap.remove(getKey(entry));
                itor.remove();
            }
        }
        this.dataStore.removeData(sessionId);
    }
    public void storeData(final String sessionId,final int id,final byte[] data){
        final Entry entry=new Entry(sessionId,id,data);
        final String key=getKey(entry);
        this.entryMap.put(key,entry);
        try{
            final boolean added=this.entries.offer(entry,30L,TimeUnit.MILLISECONDS);
            if(!added){
                AsynchronousDataStore.log.debug("Storing synchronously page with id '{}' in session '{}'",(Object)id,sessionId);
                this.entryMap.remove(key);
                this.dataStore.storeData(sessionId,id,data);
            }
        }
        catch(InterruptedException e){
            AsynchronousDataStore.log.error(e.getMessage(),e);
            this.entryMap.remove(key);
            this.dataStore.storeData(sessionId,id,data);
        }
    }
    private static String getKey(final String sessionId,final int pageId){
        return pageId+":::"+sessionId;
    }
    private static String getKey(final Entry entry){
        return getKey(entry.sessionId,entry.pageId);
    }
    static{
        log=LoggerFactory.getLogger(AsynchronousDataStore.class);
    }
    private static class Entry{
        private final String sessionId;
        private final int pageId;
        private final byte[] data;
        public Entry(final String sessionId,final int pageId,final byte[] data){
            super();
            this.sessionId=(String)Args.notNull((Object)sessionId,"sessionId");
            this.pageId=pageId;
            this.data=(byte[])Args.notNull((Object)data,"data");
        }
        public int hashCode(){
            final int prime=31;
            int result=1;
            result=31*result+this.pageId;
            result=31*result+((this.sessionId==null)?0:this.sessionId.hashCode());
            return result;
        }
        public boolean equals(final Object obj){
            if(this==obj){
                return true;
            }
            if(obj==null){
                return false;
            }
            if(this.getClass()!=obj.getClass()){
                return false;
            }
            final Entry other=(Entry)obj;
            if(this.pageId!=other.pageId){
                return false;
            }
            if(this.sessionId==null){
                if(other.sessionId!=null){
                    return false;
                }
            }
            else if(!this.sessionId.equals(other.sessionId)){
                return false;
            }
            return true;
        }
        public String toString(){
            return "Entry [sessionId="+this.sessionId+", pageId="+this.pageId+"]";
        }
    }
    private static class PageSavingRunnable implements Runnable{
        private static final Logger log;
        private final BlockingQueue<Entry> entries;
        private final ConcurrentMap<String,Entry> entryMap;
        private final IDataStore dataStore;
        private PageSavingRunnable(final IDataStore dataStore,final BlockingQueue<Entry> entries,final ConcurrentMap<String,Entry> entryMap){
            super();
            this.dataStore=dataStore;
            this.entries=entries;
            this.entryMap=entryMap;
        }
        public void run(){
            while(!Thread.interrupted()){
                Entry entry=null;
                try{
                    entry=this.entries.poll(1000L,TimeUnit.MILLISECONDS);
                }
                catch(InterruptedException e){
                    Thread.currentThread().interrupt();
                }
                if(entry!=null){
                    PageSavingRunnable.log.debug("Saving asynchronously: {}...",entry);
                    this.dataStore.storeData(entry.sessionId,entry.pageId,entry.data);
                    this.entryMap.remove(getKey(entry));
                }
            }
        }
        static{
            log=LoggerFactory.getLogger(PageSavingRunnable.class);
        }
    }
}
