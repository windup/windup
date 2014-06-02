package org.apache.wicket.page;

import java.io.*;
import java.util.*;
import org.slf4j.*;

public abstract class RequestAdapter{
    private static final Logger log;
    private final IPageManagerContext context;
    private final List<IManageablePage> touchedPages;
    public RequestAdapter(final IPageManagerContext context){
        super();
        this.touchedPages=(List<IManageablePage>)new ArrayList();
        this.context=context;
    }
    protected abstract IManageablePage getPage(final int p0);
    protected abstract void storeTouchedPages(final List<IManageablePage> p0);
    protected abstract void newSessionCreated();
    protected void bind(){
        this.context.bind();
    }
    public void setSessionAttribute(final String key,final Serializable value){
        this.context.setSessionAttribute(key,value);
    }
    public Serializable getSessionAttribute(final String key){
        return this.context.getSessionAttribute(key);
    }
    public String getSessionId(){
        return this.context.getSessionId();
    }
    protected IManageablePage findPage(final int id){
        for(final IManageablePage page : this.touchedPages){
            if(page.getPageId()==id){
                return page;
            }
        }
        return null;
    }
    protected void touch(final IManageablePage page){
        if(this.findPage(page.getPageId())==null){
            this.touchedPages.add(page);
        }
    }
    protected void commitRequest(){
        if(!this.touchedPages.isEmpty()){
            final List<IManageablePage> statefulPages=(List<IManageablePage>)new ArrayList(this.touchedPages.size());
            for(final IManageablePage page : this.touchedPages){
                try{
                    page.detach();
                }
                catch(Exception e){
                    RequestAdapter.log.error("Error detaching page",e);
                }
                if(!page.isPageStateless()){
                    statefulPages.add(page);
                }
            }
            if(!statefulPages.isEmpty()){
                this.storeTouchedPages(statefulPages);
            }
        }
    }
    static{
        log=LoggerFactory.getLogger(RequestAdapter.class);
    }
}
