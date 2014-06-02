package org.apache.wicket.mock;

import java.util.*;
import org.apache.wicket.page.*;

public class MockPageManager implements IPageManager{
    private final Map<Integer,IManageablePage> pages;
    public MockPageManager(){
        super();
        this.pages=(Map<Integer,IManageablePage>)new HashMap();
    }
    public void commitRequest(){
    }
    public void destroy(){
        this.pages.clear();
    }
    public IManageablePage getPage(final int id){
        return (IManageablePage)this.pages.get(id);
    }
    public void newSessionCreated(){
        this.pages.clear();
    }
    public void sessionExpired(final String sessionId){
        this.pages.clear();
    }
    public void setContext(final IPageManagerContext context){
    }
    public boolean supportsVersioning(){
        return true;
    }
    public void touchPage(final IManageablePage page){
        this.pages.put(page.getPageId(),page);
    }
    public IPageManagerContext getContext(){
        return null;
    }
}
