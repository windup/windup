package org.apache.wicket.page;

import org.apache.wicket.pageStore.*;
import java.util.concurrent.*;
import java.util.*;
import java.io.*;

public class PageStoreManager extends AbstractPageManager{
    private static final ConcurrentMap<String,PageStoreManager> managers;
    private final IPageStore pageStore;
    private final String applicationName;
    public PageStoreManager(final String applicationName,final IPageStore pageStore,final IPageManagerContext context){
        super(context);
        this.applicationName=applicationName;
        this.pageStore=pageStore;
        if(PageStoreManager.managers.containsKey(applicationName)){
            throw new IllegalStateException("Manager for application with key '"+applicationName+"' already exists.");
        }
        PageStoreManager.managers.put(applicationName,this);
    }
    protected RequestAdapter newRequestAdapter(final IPageManagerContext context){
        return new PersistentRequestAdapter(context);
    }
    public boolean supportsVersioning(){
        return true;
    }
    public void sessionExpired(final String sessionId){
        this.pageStore.unbind(sessionId);
    }
    public void destroy(){
        PageStoreManager.managers.remove(this.applicationName);
        this.pageStore.destroy();
    }
    static{
        managers=new ConcurrentHashMap<String,PageStoreManager>();
    }
    private static class SessionEntry implements Serializable{
        private static final long serialVersionUID=1L;
        private final String applicationName;
        private final String sessionId;
        private transient List<IManageablePage> sessionCache;
        private transient List<Object> afterReadObject;
        public SessionEntry(final String applicationName,final String sessionId){
            super();
            this.applicationName=applicationName;
            this.sessionId=sessionId;
        }
        private IPageStore getPageStore(){
            final PageStoreManager manager=(PageStoreManager)PageStoreManager.managers.get(this.applicationName);
            if(manager==null){
                return null;
            }
            return manager.pageStore;
        }
        private IManageablePage findPage(final int id){
            for(final IManageablePage p : this.sessionCache){
                if(p.getPageId()==id){
                    return p;
                }
            }
            return null;
        }
        private void addPage(final IManageablePage page){
            if(page!=null){
                if(this.findPage(page.getPageId())!=null){
                    return;
                }
                this.sessionCache.add(page);
            }
        }
        private void convertAfterReadObjects(){
            if(this.sessionCache==null){
                this.sessionCache=(List<IManageablePage>)new ArrayList();
            }
            for(final Object o : this.afterReadObject){
                final IManageablePage page=this.getPageStore().convertToPage(o);
                this.addPage(page);
            }
            this.afterReadObject=null;
        }
        public synchronized IManageablePage getPage(final int id){
            if(this.afterReadObject!=null&&!this.afterReadObject.isEmpty()){
                this.convertAfterReadObjects();
            }
            if(this.sessionCache!=null){
                final IManageablePage page=this.findPage(id);
                if(page!=null){
                    return page;
                }
            }
            return this.getPageStore().getPage(this.sessionId,id);
        }
        public synchronized void setSessionCache(final List<IManageablePage> pages){
            this.sessionCache=(List<IManageablePage>)new ArrayList(pages);
            this.afterReadObject=null;
        }
        private void writeObject(final ObjectOutputStream s) throws IOException{
            s.defaultWriteObject();
            final List<Serializable> serializedPages=(List<Serializable>)new ArrayList();
            if(this.sessionCache!=null){
                final IPageStore pageStore=this.getPageStore();
                for(final IManageablePage p : this.sessionCache){
                    Serializable preparedPage;
                    if(pageStore!=null){
                        preparedPage=pageStore.prepareForSerialization(this.sessionId,p);
                    }
                    else{
                        preparedPage=(Serializable)p;
                    }
                    if(preparedPage!=null){
                        serializedPages.add(preparedPage);
                    }
                }
            }
            s.writeObject(serializedPages);
        }
        private void readObject(final ObjectInputStream s) throws IOException,ClassNotFoundException{
            s.defaultReadObject();
            this.afterReadObject=(List<Object>)new ArrayList();
            final List<Serializable> l=(List<Serializable>)s.readObject();
            final IPageStore pageStore=this.getPageStore();
            for(final Serializable ser : l){
                Object page;
                if(pageStore!=null){
                    page=pageStore.restoreAfterSerialization(ser);
                }
                else{
                    page=ser;
                }
                this.afterReadObject.add(page);
            }
        }
    }
    protected class PersistentRequestAdapter extends RequestAdapter{
        private static final String ATTRIBUTE_NAME="wicket:persistentPageManagerData";
        private String getAttributeName(){
            return "wicket:persistentPageManagerData - "+PageStoreManager.this.applicationName;
        }
        public PersistentRequestAdapter(final IPageManagerContext context){
            super(context);
        }
        protected IManageablePage getPage(final int id){
            final IManageablePage touchedPage=this.findPage(id);
            if(touchedPage!=null){
                return touchedPage;
            }
            final SessionEntry entry=this.getSessionEntry(false);
            if(entry!=null){
                return entry.getPage(id);
            }
            return null;
        }
        private SessionEntry getSessionEntry(final boolean create){
            SessionEntry entry=(SessionEntry)this.getSessionAttribute(this.getAttributeName());
            if(entry==null&&create){
                this.bind();
                entry=new SessionEntry(PageStoreManager.this.applicationName,this.getSessionId());
            }
            if(entry!=null){
                synchronized(entry){
                    this.setSessionAttribute(this.getAttributeName(),entry);
                }
            }
            return entry;
        }
        protected void newSessionCreated(){
            if(this.getSessionId()!=null){
                this.getSessionEntry(true);
            }
        }
        protected void storeTouchedPages(final List<IManageablePage> touchedPages){
            if(!touchedPages.isEmpty()){
                final SessionEntry entry=this.getSessionEntry(true);
                entry.setSessionCache(touchedPages);
                for(final IManageablePage page : touchedPages){
                    PageStoreManager.this.pageStore.storePage(this.getSessionId(),page);
                }
            }
        }
    }
}
