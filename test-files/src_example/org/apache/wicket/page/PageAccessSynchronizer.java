package org.apache.wicket.page;

import java.io.*;
import org.apache.wicket.util.*;
import java.util.concurrent.*;
import org.apache.wicket.util.time.*;
import org.apache.wicket.util.value.*;
import org.apache.wicket.*;
import org.apache.wicket.util.lang.*;
import org.apache.wicket.settings.*;
import java.util.*;
import org.slf4j.*;

public class PageAccessSynchronizer implements Serializable{
    private static final long serialVersionUID=1L;
    private static final Logger logger;
    private final IProvider<ConcurrentMap<Integer,PageLock>> locks;
    private final Duration timeout;
    public PageAccessSynchronizer(final Duration timeout){
        super();
        this.locks=(IProvider<ConcurrentMap<Integer,PageLock>>)new LazyInitializer<ConcurrentMap<Integer,PageLock>>(){
            private static final long serialVersionUID=1L;
            protected ConcurrentMap<Integer,PageLock> createInstance(){
                return new ConcurrentHashMap<Integer,PageLock>();
            }
        };
        this.timeout=timeout;
    }
    private static long remaining(final Time start,final Duration timeout){
        return Math.max(0L,timeout.subtract(start.elapsedSince()).getMilliseconds());
    }
    public void lockPage(final int pageId) throws CouldNotLockPageException{
        final Thread thread=Thread.currentThread();
        final PageLock lock=new PageLock(pageId,thread);
        final Time start=Time.now();
        boolean locked=false;
        final boolean isDebugEnabled=PageAccessSynchronizer.logger.isDebugEnabled();
        PageLock previous=null;
        while(!locked&&start.elapsedSince().lessThan((LongValue)this.timeout)){
            if(isDebugEnabled){
                PageAccessSynchronizer.logger.debug("'{}' attempting to acquire lock to page with id '{}'",thread.getName(),pageId);
            }
            previous=((ConcurrentMap)this.locks.get()).putIfAbsent(pageId,lock);
            if(previous==null||previous.thread==thread){
                locked=true;
            }
            else{
                final long remaining=remaining(start,this.timeout);
                if(remaining<=0L){
                    continue;
                }
                synchronized(previous){
                    if(isDebugEnabled){
                        PageAccessSynchronizer.logger.debug("{} waiting for lock to page {} for {}",thread.getName(),pageId,Duration.milliseconds(remaining));
                    }
                    try{
                        previous.wait(remaining);
                    }
                    catch(InterruptedException e){
                        throw new RuntimeException((Throwable)e);
                    }
                }
            }
        }
        if(locked){
            if(isDebugEnabled){
                PageAccessSynchronizer.logger.debug("{} acquired lock to page {}",thread.getName(),pageId);
            }
            return;
        }
        if(PageAccessSynchronizer.logger.isWarnEnabled()){
            PageAccessSynchronizer.logger.warn("Thread '{}' failed to acquire lock to page with id '{}', attempted for {} out of allowed {}. The thread that holds the lock has name '{}'.",thread.getName(),pageId,start.elapsedSince(),this.timeout,previous.thread.getName());
            if(Application.exists()){
                final IExceptionSettings.ThreadDumpStrategy strategy=Application.get().getExceptionSettings().getThreadDumpStrategy();
                switch(strategy){
                    case ALL_THREADS:{
                        Threads.dumpAllThreads(PageAccessSynchronizer.logger);
                        break;
                    }
                    case THREAD_HOLDING_LOCK:{
                        Threads.dumpSingleThread(PageAccessSynchronizer.logger,previous.thread);
                        break;
                    }
                }
            }
        }
        throw new CouldNotLockPageException(pageId,thread.getName(),this.timeout);
    }
    public void unlockAllPages(){
        this.internalUnlockPages(null);
    }
    public void unlockPage(final int pageId){
        this.internalUnlockPages(pageId);
    }
    private void internalUnlockPages(final Integer pageId){
        final Thread thread=Thread.currentThread();
        final Iterator<PageLock> locks=(Iterator<PageLock>)((ConcurrentMap)this.locks.get()).values().iterator();
        final boolean isDebugEnabled=PageAccessSynchronizer.logger.isDebugEnabled();
        while(locks.hasNext()){
            final PageLock lock=(PageLock)locks.next();
            if((pageId==null||pageId==lock.pageId)&&lock.thread==thread){
                locks.remove();
                if(isDebugEnabled){
                    PageAccessSynchronizer.logger.debug("'{}' released lock to page with id '{}'",thread.getName(),lock.pageId);
                }
                synchronized(lock){
                    if(isDebugEnabled){
                        PageAccessSynchronizer.logger.debug("'{}' notifying blocked threads",thread.getName());
                    }
                    lock.notifyAll();
                }
                if(pageId!=null){
                    break;
                }
                continue;
            }
        }
    }
    IProvider<ConcurrentMap<Integer,PageLock>> getLocks(){
        return this.locks;
    }
    public IPageManager adapt(final IPageManager pagemanager){
        return new PageManagerDecorator(pagemanager){
            public IManageablePage getPage(final int pageId){
                IManageablePage page=null;
                try{
                    PageAccessSynchronizer.this.lockPage(pageId);
                    page=super.getPage(pageId);
                }
                finally{
                    if(page==null){
                        PageAccessSynchronizer.this.unlockPage(pageId);
                    }
                }
                return page;
            }
            public void touchPage(final IManageablePage page){
                PageAccessSynchronizer.this.lockPage(page.getPageId());
                super.touchPage(page);
            }
            public void commitRequest(){
                try{
                    super.commitRequest();
                }
                finally{
                    PageAccessSynchronizer.this.unlockAllPages();
                }
            }
        };
    }
    static{
        logger=LoggerFactory.getLogger(PageAccessSynchronizer.class);
    }
    public static class PageLock{
        private final int pageId;
        private final Thread thread;
        public PageLock(final int pageId,final Thread thread){
            super();
            this.pageId=pageId;
            this.thread=thread;
        }
        public int getPageId(){
            return this.pageId;
        }
        public Thread getThread(){
            return this.thread;
        }
    }
}
