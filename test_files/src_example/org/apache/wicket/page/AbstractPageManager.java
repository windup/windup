package org.apache.wicket.page;

import org.apache.wicket.util.lang.*;
import org.slf4j.*;

public abstract class AbstractPageManager implements IPageManager{
    private static final Logger log;
    private final IPageManagerContext context;
    public AbstractPageManager(final IPageManagerContext context){
        super();
        this.context=(IPageManagerContext)Args.notNull((Object)context,"context");
    }
    protected abstract RequestAdapter newRequestAdapter(final IPageManagerContext p0);
    public abstract boolean supportsVersioning();
    public abstract void sessionExpired(final String p0);
    public IPageManagerContext getContext(){
        return this.context;
    }
    protected RequestAdapter getRequestAdapter(){
        RequestAdapter adapter=(RequestAdapter)this.getContext().getRequestData();
        if(adapter==null){
            adapter=this.newRequestAdapter(this.getContext());
            this.getContext().setRequestData(adapter);
        }
        return adapter;
    }
    public void commitRequest(){
        this.getRequestAdapter().commitRequest();
    }
    public IManageablePage getPage(final int id){
        final IManageablePage page=this.getRequestAdapter().getPage(id);
        if(page!=null){
            this.getRequestAdapter().touch(page);
        }
        return page;
    }
    public void newSessionCreated(){
        this.getRequestAdapter().newSessionCreated();
    }
    public void touchPage(final IManageablePage page){
        if(!page.isPageStateless()){
            this.getContext().bind();
        }
        this.getRequestAdapter().touch(page);
    }
    static{
        log=LoggerFactory.getLogger(AbstractPageManager.class);
    }
}
