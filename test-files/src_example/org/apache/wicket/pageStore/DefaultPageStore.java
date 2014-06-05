package org.apache.wicket.pageStore;

import org.apache.wicket.serialize.*;
import org.apache.wicket.page.*;
import java.io.*;
import org.slf4j.*;
import org.apache.wicket.util.lang.*;
import java.lang.ref.*;
import java.util.*;

public class DefaultPageStore implements IPageStore{
    private static final Logger LOG;
    private final SerializedPagesCache serializedPagesCache;
    private final IDataStore pageDataStore;
    private final ISerializer pageSerializer;
    public DefaultPageStore(final ISerializer pageSerializer,final IDataStore dataStore,final int cacheSize){
        super();
        Args.notNull((Object)pageSerializer,"pageSerializer");
        Args.notNull((Object)dataStore,"DataStore");
        this.pageSerializer=pageSerializer;
        this.pageDataStore=dataStore;
        this.serializedPagesCache=new SerializedPagesCache(cacheSize);
    }
    public void destroy(){
        this.pageDataStore.destroy();
    }
    protected byte[] getPageData(final String sessionId,final int pageId){
        return this.pageDataStore.getData(sessionId,pageId);
    }
    protected void removePageData(final String sessionId,final int pageId){
        this.pageDataStore.removeData(sessionId,pageId);
    }
    protected void removePageData(final String sessionId){
        this.pageDataStore.removeData(sessionId);
    }
    protected void storePageData(final String sessionId,final int pageId,final byte[] data){
        this.pageDataStore.storeData(sessionId,pageId,data);
    }
    public IManageablePage getPage(final String sessionId,final int id){
        final SerializedPage fromCache=this.serializedPagesCache.getPage(sessionId,id);
        if(fromCache!=null&&fromCache.data!=null){
            return this.deserializePage(fromCache.data);
        }
        final byte[] data=this.getPageData(sessionId,id);
        if(data!=null){
            return this.deserializePage(data);
        }
        return null;
    }
    public void removePage(final String sessionId,final int id){
        this.serializedPagesCache.removePage(sessionId,id);
        this.removePageData(sessionId,id);
    }
    public void storePage(final String sessionId,final IManageablePage page){
        final SerializedPage serialized=this.serializePage(sessionId,page);
        if(serialized!=null){
            this.serializedPagesCache.storePage(serialized);
            this.storePageData(sessionId,serialized.getPageId(),serialized.getData());
        }
    }
    public void unbind(final String sessionId){
        this.removePageData(sessionId);
        this.serializedPagesCache.removePages(sessionId);
    }
    public IManageablePage convertToPage(final Object object){
        if(object==null){
            return null;
        }
        if(object instanceof IManageablePage){
            return (IManageablePage)object;
        }
        if(!(object instanceof SerializedPage)){
            final String type=object.getClass().getName();
            throw new IllegalArgumentException("Unknown object type "+type);
        }
        final SerializedPage page=(SerializedPage)object;
        byte[] data=page.getData();
        if(data==null){
            data=this.getPageData(page.getSessionId(),page.getPageId());
        }
        if(data!=null){
            return this.deserializePage(data);
        }
        return null;
    }
    private SerializedPage restoreStrippedSerializedPage(final SerializedPage serializedPage){
        final SerializedPage result=this.serializedPagesCache.getPage(serializedPage.getSessionId(),serializedPage.getPageId());
        if(result!=null){
            return result;
        }
        final byte[] data=this.getPageData(serializedPage.getSessionId(),serializedPage.getPageId());
        return new SerializedPage(serializedPage.getSessionId(),serializedPage.getPageId(),data);
    }
    public Serializable prepareForSerialization(final String sessionId,final Object object){
        if(this.pageDataStore.isReplicated()){
            return null;
        }
        SerializedPage result=null;
        if(object instanceof IManageablePage){
            final IManageablePage page=(IManageablePage)object;
            result=this.serializedPagesCache.getPage(sessionId,page.getPageId());
            if(result==null){
                result=this.serializePage(sessionId,page);
                if(result!=null){
                    this.serializedPagesCache.storePage(result);
                }
            }
        }
        else if(object instanceof SerializedPage){
            final SerializedPage page2=(SerializedPage)object;
            if(page2.getData()==null){
                result=this.restoreStrippedSerializedPage(page2);
            }
            else{
                result=page2;
            }
        }
        if(result!=null){
            return result;
        }
        return (Serializable)object;
    }
    protected boolean storeAfterSessionReplication(){
        return true;
    }
    public Object restoreAfterSerialization(final Serializable serializable){
        if(serializable==null){
            return null;
        }
        if(!this.storeAfterSessionReplication()||serializable instanceof IManageablePage){
            return serializable;
        }
        if(!(serializable instanceof SerializedPage)){
            final String type=serializable.getClass().getName();
            throw new IllegalArgumentException("Unknown object type "+type);
        }
        final SerializedPage page=(SerializedPage)serializable;
        if(page.getData()!=null){
            this.storePageData(page.getSessionId(),page.getPageId(),page.getData());
            return new SerializedPage(page.getSessionId(),page.getPageId(),null);
        }
        return page;
    }
    protected SerializedPage serializePage(final String sessionId,final IManageablePage page){
        Args.notNull((Object)sessionId,"sessionId");
        Args.notNull((Object)page,"page");
        SerializedPage serializedPage=null;
        final byte[] data=this.pageSerializer.serialize((Object)page);
        if(data!=null){
            serializedPage=new SerializedPage(sessionId,page.getPageId(),data);
        }
        else{
            DefaultPageStore.LOG.warn("Page {} cannot be serialized. See previous logs for possible reasons.",page);
        }
        return serializedPage;
    }
    protected IManageablePage deserializePage(final byte[] data){
        final IManageablePage page=(IManageablePage)this.pageSerializer.deserialize(data);
        return page;
    }
    static{
        LOG=LoggerFactory.getLogger(DefaultPageStore.class);
    }
    protected static class SerializedPage implements Serializable{
        private static final long serialVersionUID=1L;
        private final int pageId;
        private final String sessionId;
        private final byte[] data;
        public SerializedPage(final String sessionId,final int pageId,final byte[] data){
            super();
            this.pageId=pageId;
            this.sessionId=sessionId;
            this.data=data;
        }
        public byte[] getData(){
            return this.data;
        }
        public int getPageId(){
            return this.pageId;
        }
        public String getSessionId(){
            return this.sessionId;
        }
        public boolean equals(final Object obj){
            if(this==obj){
                return true;
            }
            if(!(obj instanceof SerializedPage)){
                return false;
            }
            final SerializedPage rhs=(SerializedPage)obj;
            return Objects.equal((Object)this.getPageId(),(Object)rhs.getPageId())&&Objects.equal((Object)this.getSessionId(),(Object)rhs.getSessionId());
        }
        public int hashCode(){
            return Objects.hashCode(new Object[] { this.getPageId(),this.getSessionId() });
        }
    }
    static class SerializedPagesCache{
        private final int size;
        private final List<SoftReference<SerializedPage>> cache;
        public SerializedPagesCache(final int size){
            super();
            this.size=size;
            this.cache=(List<SoftReference<SerializedPage>>)new ArrayList(size);
        }
        public SerializedPage removePage(final String sessionId,final int id){
            Args.notNull((Object)sessionId,"sessionId");
            if(this.size>0){
                synchronized(this.cache){
                    final Iterator<SoftReference<SerializedPage>> i=(Iterator<SoftReference<SerializedPage>>)this.cache.iterator();
                    while(i.hasNext()){
                        final SoftReference<SerializedPage> ref=(SoftReference<SerializedPage>)i.next();
                        final SerializedPage entry=(SerializedPage)ref.get();
                        if(entry!=null&&entry.getPageId()==id&&entry.getSessionId().equals(sessionId)){
                            i.remove();
                            return entry;
                        }
                    }
                }
            }
            return null;
        }
        public void removePages(final String sessionId){
            Args.notNull((Object)sessionId,"sessionId");
            if(this.size>0){
                synchronized(this.cache){
                    final Iterator<SoftReference<SerializedPage>> i=(Iterator<SoftReference<SerializedPage>>)this.cache.iterator();
                    while(i.hasNext()){
                        final SoftReference<SerializedPage> ref=(SoftReference<SerializedPage>)i.next();
                        final SerializedPage entry=(SerializedPage)ref.get();
                        if(entry!=null&&entry.getSessionId().equals(sessionId)){
                            i.remove();
                        }
                    }
                }
            }
        }
        public SerializedPage getPage(final String sessionId,final int pageId){
            Args.notNull((Object)sessionId,"sessionId");
            SerializedPage result=null;
            if(this.size>0){
                synchronized(this.cache){
                    final Iterator<SoftReference<SerializedPage>> i=(Iterator<SoftReference<SerializedPage>>)this.cache.iterator();
                    while(i.hasNext()){
                        final SoftReference<SerializedPage> ref=(SoftReference<SerializedPage>)i.next();
                        final SerializedPage entry=(SerializedPage)ref.get();
                        if(entry!=null&&entry.getPageId()==pageId&&entry.getSessionId().equals(sessionId)){
                            i.remove();
                            result=entry;
                            break;
                        }
                    }
                    if(result!=null){
                        this.storePage(result);
                    }
                }
            }
            return result;
        }
        void storePage(final SerializedPage page){
            final SoftReference<SerializedPage> ref=(SoftReference<SerializedPage>)new SoftReference(page);
            if(this.size>0){
                synchronized(this.cache){
                    final Iterator<SoftReference<SerializedPage>> i=(Iterator<SoftReference<SerializedPage>>)this.cache.iterator();
                    while(i.hasNext()){
                        final SoftReference<SerializedPage> r=(SoftReference<SerializedPage>)i.next();
                        final SerializedPage entry=(SerializedPage)r.get();
                        if(entry!=null&&entry.equals(page)){
                            i.remove();
                            break;
                        }
                    }
                    this.cache.add(ref);
                    if(this.cache.size()>this.size){
                        this.cache.remove(0);
                    }
                }
            }
        }
    }
}
