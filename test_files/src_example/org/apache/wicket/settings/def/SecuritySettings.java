package org.apache.wicket.settings.def;

import org.apache.wicket.settings.*;
import org.apache.wicket.authentication.*;
import org.apache.wicket.*;
import org.apache.wicket.authorization.*;
import org.apache.wicket.util.crypt.*;
import org.apache.wicket.authentication.strategy.*;

public class SecuritySettings implements ISecuritySettings{
    private IAuthorizationStrategy authorizationStrategy;
    private IAuthenticationStrategy authenticationStrategy;
    private ICryptFactory cryptFactory;
    private boolean enforceMounts;
    private IUnauthorizedComponentInstantiationListener unauthorizedComponentInstantiationListener;
    public SecuritySettings(){
        super();
        this.authorizationStrategy=IAuthorizationStrategy.ALLOW_ALL;
        this.enforceMounts=false;
        this.unauthorizedComponentInstantiationListener=new IUnauthorizedComponentInstantiationListener(){
            public void onUnauthorizedInstantiation(final Component component){
                throw new UnauthorizedInstantiationException((Class<T>)component.getClass());
            }
        };
    }
    public IAuthorizationStrategy getAuthorizationStrategy(){
        return this.authorizationStrategy;
    }
    public synchronized ICryptFactory getCryptFactory(){
        if(this.cryptFactory==null){
            this.cryptFactory=(ICryptFactory)new CachingSunJceCryptFactory("WiCkEt-FRAMEwork");
        }
        return this.cryptFactory;
    }
    public boolean getEnforceMounts(){
        return this.enforceMounts;
    }
    public IUnauthorizedComponentInstantiationListener getUnauthorizedComponentInstantiationListener(){
        return this.unauthorizedComponentInstantiationListener;
    }
    public void setAuthorizationStrategy(final IAuthorizationStrategy strategy){
        if(strategy==null){
            throw new IllegalArgumentException("authorization strategy cannot be set to null");
        }
        this.authorizationStrategy=strategy;
    }
    public void setCryptFactory(final ICryptFactory cryptFactory){
        if(cryptFactory==null){
            throw new IllegalArgumentException("cryptFactory cannot be null");
        }
        this.cryptFactory=cryptFactory;
    }
    public void setEnforceMounts(final boolean enforce){
        this.enforceMounts=enforce;
    }
    public void setUnauthorizedComponentInstantiationListener(final IUnauthorizedComponentInstantiationListener unauthorizedComponentInstantiationListener){
        this.unauthorizedComponentInstantiationListener=unauthorizedComponentInstantiationListener;
    }
    public IAuthenticationStrategy getAuthenticationStrategy(){
        if(this.authenticationStrategy==null){
            this.authenticationStrategy=new DefaultAuthenticationStrategy("LoggedIn");
        }
        return this.authenticationStrategy;
    }
    public void setAuthenticationStrategy(final IAuthenticationStrategy strategy){
        this.authenticationStrategy=strategy;
    }
}
