package org.apache.wicket.authorization.strategies.page;

import java.lang.ref.*;
import org.apache.wicket.*;
import org.apache.wicket.authorization.*;

public abstract class SimplePageAuthorizationStrategy extends AbstractPageAuthorizationStrategy{
    private final WeakReference<Class<?>> securePageSuperTypeRef;
    public SimplePageAuthorizationStrategy(final Class<?> securePageSuperType,final Class<S> signInPageClass){
        super();
        if(securePageSuperType==null){
            throw new IllegalArgumentException("Secure page super type must not be null");
        }
        this.securePageSuperTypeRef=(WeakReference<Class<?>>)new WeakReference(securePageSuperType);
        Application.get().getSecuritySettings().setUnauthorizedComponentInstantiationListener(new IUnauthorizedComponentInstantiationListener(){
            public void onUnauthorizedInstantiation(final Component component){
                if(component instanceof Page){
                    throw new RestartResponseAtInterceptPageException(signInPageClass);
                }
                throw new UnauthorizedInstantiationException((Class<T>)component.getClass());
            }
        });
    }
    protected <T extends Page> boolean isPageAuthorized(final Class<T> pageClass){
        return !this.instanceOf(pageClass,(Class<?>)this.securePageSuperTypeRef.get())||this.isAuthorized();
    }
    protected abstract boolean isAuthorized();
}
