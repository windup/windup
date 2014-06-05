package org.apache.wicket.mock;

import org.apache.wicket.protocol.http.*;
import org.apache.wicket.request.*;
import org.apache.wicket.util.*;
import org.apache.wicket.session.*;
import org.apache.wicket.*;
import org.apache.wicket.request.resource.caching.*;
import org.apache.wicket.page.*;

public class MockApplication extends WebApplication{
    public Class<? extends Page> getHomePage(){
        return (Class<? extends Page>)MockHomePage.class;
    }
    public RuntimeConfigurationType getConfigurationType(){
        return RuntimeConfigurationType.DEVELOPMENT;
    }
    public Session getSession(){
        return this.getSessionStore().lookup(null);
    }
    public final String getInitParameter(final String key){
        return null;
    }
    protected void internalInit(){
        super.internalInit();
        this.setSessionStoreProvider((IProvider<ISessionStore>)new MockSessionStoreProvider());
        this.setPageManagerProvider(new MockPageManagerProvider());
        this.getResourceSettings().setCachingStrategy(NoOpResourceCachingStrategy.INSTANCE);
    }
    private static class MockSessionStoreProvider implements IProvider<ISessionStore>{
        public ISessionStore get(){
            return new MockSessionStore();
        }
    }
    private static class MockPageManagerProvider implements IPageManagerProvider{
        public IPageManager get(final IPageManagerContext pageManagerContext){
            return new MockPageManager();
        }
    }
}
