package org.apache.wicket;

import org.apache.wicket.request.mapper.*;
import org.apache.wicket.request.resource.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.request.mapper.parameter.*;
import org.apache.wicket.page.*;

public class DefaultMapperContext implements IMapperContext{
    public String getBookmarkableIdentifier(){
        return "bookmarkable";
    }
    public String getNamespace(){
        return "wicket";
    }
    public String getPageIdentifier(){
        return "page";
    }
    public String getResourceIdentifier(){
        return "resource";
    }
    public ResourceReferenceRegistry getResourceReferenceRegistry(){
        return Application.get().getResourceReferenceRegistry();
    }
    public RequestListenerInterface requestListenerInterfaceFromString(final String interfaceName){
        return RequestListenerInterface.forName(interfaceName);
    }
    public String requestListenerInterfaceToString(final RequestListenerInterface listenerInterface){
        return listenerInterface.getName();
    }
    public IRequestablePage newPageInstance(final Class<? extends IRequestablePage> pageClass,final PageParameters pageParameters){
        if(pageParameters==null){
            return Application.get().getPageFactory().newPage(pageClass);
        }
        return Application.get().getPageFactory().newPage(pageClass,pageParameters);
    }
    public IRequestablePage getPageInstance(final int pageId){
        final IManageablePage manageablePage=Session.get().getPageManager().getPage(pageId);
        IRequestablePage requestablePage=null;
        if(manageablePage instanceof IRequestablePage){
            requestablePage=(IRequestablePage)manageablePage;
        }
        return requestablePage;
    }
    public Class<? extends IRequestablePage> getHomePageClass(){
        return Application.get().getHomePage();
    }
}
