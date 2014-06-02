package org.apache.wicket.authorization.strategies.page;

import org.apache.wicket.authorization.*;
import org.apache.wicket.request.component.*;
import org.apache.wicket.*;

public abstract class AbstractPageAuthorizationStrategy implements IAuthorizationStrategy{
    public boolean isActionAuthorized(final Component component,final Action action){
        return true;
    }
    public final <T extends IRequestableComponent> boolean isInstantiationAuthorized(final Class<T> componentClass){
        return !this.instanceOf(componentClass,(Class<?>)Page.class)||this.isPageAuthorized((Class<Page>)componentClass);
    }
    protected boolean instanceOf(final Class<?> type,final Class<?> superType){
        return superType!=null&&superType.isAssignableFrom(type);
    }
    protected <T extends Page> boolean isPageAuthorized(final Class<T> pageClass){
        return true;
    }
}
