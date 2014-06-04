package org.apache.wicket.settings.def;

import org.apache.wicket.settings.*;
import org.apache.wicket.*;

public class SessionSettings implements ISessionSettings{
    @Deprecated
    public IPageFactory getPageFactory(){
        IPageFactory pageFactory=null;
        if(Application.exists()){
            pageFactory=Application.get().getPageFactory();
        }
        return pageFactory;
    }
    @Deprecated
    public void setPageFactory(final IPageFactory defaultPageFactory){
    }
}
